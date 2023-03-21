package dev.enbnt.timeseries.util

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.varargs
import scala.collection.IndexedSeqView
import scala.collection.IterableFactory
import scala.collection.IterableFactoryDefaults
import scala.collection.IterableOps
import scala.collection.StrictOptimizedIterableOps
import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * A mutable bounded buffer implementation, which behaves as a circle/ring for
 * sequential read/write access. The underlying buffer uses two pointers &mdash;
 * a read-index and a write-index, which are used to determine the logical
 * beginning, ending, and size of the unconsumed buffer.
 *
 * @example
 *   // format: off
 *   {{{
 *     val ring = new ConcurrentCircularBuffer[String](2)  // ring: [_, _] read: 0 write: 0 size: 0
 *     ring.write("hello") // ring: ["hello", _] read: 0 write: 1 size: 1
 *     ring.write("world") // ring: ["hello", "world"] read: 0 write: 0 size: 2
 *
 *     ring.read() // "hello" - ring: [_, "world"] read: 1 write: 0 size: 1
 *     ring.read() // "world" - ring: [_, _] read: 0 write: 0 size: 0
 *
 *     // calling ring.read() again would throw an exception
 *
 *     ring.write("good") // ring: ["good", _] read: 0 write: 1 size: 1
 *     ring.write("bye") // ring: ["good", "bye"] read: 0 write: 0 size: 2
 *     ring.write("sunshine") // ring: ["sunshine", "bye"] read: 1 write: 1 size: 2
 *
 *     ring.read() // "bye" - ring: ["sunshine", _] read: 0 write: 1 size: 1
 *     ring.read() // "sunshine" - ring: [_, _] read: 1 write: 1 size: 0
 *   }}}
 *
 * @note
 *   This implementation <b>IS</b> thread safe.
 *
 * @note
 *   The only methods that modify the the buffer's indices are [[read()]] and
 *   [[write()]]. Any other methods which access data in the buffer via an
 *   offset will not mutate the underlying buffer state.
 *
 * This is a **VERY IMPORTANT** detail, as access via iterators or collection
 * methods (map, flatMap) will act over a view of the buffer data, but not
 * modify its state directly. This allows the data in the buffer to be accessed
 * *without* emptying or mutating the state of the buffer as a result.
 *
 * @see
 *   [[https://en.wikipedia.org/wiki/Circular_buffer]]
 * @see
 *   [[https://docs.scala-lang.org/overviews/core/custom-collections.html]]
 *
 * @param capacity
 *   The maximum allowed size of the underlying buffer.
 * @param elems
 *   The defined buffer elements.
 * @param readIdx
 *   The starting offset of the read index
 * @param writeIdx
 *   The starting offset of the write index
 * @tparam T
 *   The type of elements of contained within the buffer
 */
private[timeseries] class ConcurrentCircularBuffer[T: ClassTag] private (
    val capacity: Int,
    elems: Array[T],
    val readIdx: AtomicInteger,
    val writeIdx: AtomicInteger
) extends Iterable[T]
    with IterableOps[T, ConcurrentCircularBuffer, ConcurrentCircularBuffer[T]]
    with IterableFactoryDefaults[T, ConcurrentCircularBuffer]
    with StrictOptimizedIterableOps[
      T,
      ConcurrentCircularBuffer,
      ConcurrentCircularBuffer[T]
    ] {
  self =>

  require(
    capacity > 0,
    s"ConcurrentCircularBuffer capacity must be > 0, but received '$capacity'"
  )

  private[this] val readLatch = new Semaphore(readWriteDelta)

  private[this] def count: Int =
    math.min(readWriteDelta, capacity)

  private[this] def readWriteDelta: Int = synchronized {
    writeIdx.get - readIdx.get
  } + 1

  def this(capacity: Int, elems: Array[T], readIdx: Int, writeIdx: Int) = this(
    capacity,
    elems,
    new AtomicInteger(readIdx),
    new AtomicInteger(writeIdx)
  )

  def this(capacity: Int) =
    this(capacity, elems = Array.ofDim[T](capacity), readIdx = 0, writeIdx = -1)

  /**
   * Consume the value at the current read index, modifying the state of the
   * buffer. The thread will block indefinitely until a value has been written.
   */
  @throws[IllegalStateException]
  def read(): T = {
    readLatch.tryAcquire()
    elems(readIdx.getAndIncrement() % capacity)
  }

  /**
   * Reads the value at the offset of the current read index position,
   * accounting for loops in the buffer. An [[IllegalStateException]] will be
   * thrown if the [[offset]] is too large for the state of the buffer.
   *
   * @note
   *   Calling this method does not modify the read index position or the state
   *   of the buffer.
   *
   * @param offset
   *   The logical index in the buffer to read from
   *
   * @return
   *   The element at logical index [[offset]]
   */
  @throws[IllegalStateException]
  def read(offset: Int): T = {
    if (offset >= size)
      throw new IllegalStateException(
        s"Read offset '$offset' was >= buffer size of '$size'"
      )
    elems((readIdx.get() + offset) % capacity)
  }

  /**
   * Set the value at the current write index, modifying the state of the
   * buffer.
   */
  def write(value: T): Unit = {
    elems(writeIdx.incrementAndGet() % capacity) = value
    if (writeIdx.get() - readIdx.get() >= capacity) {
      readIdx.incrementAndGet()
    }
    readLatch.release()
  }

  def write(offset: Int, value: T): Unit = {
    if (offset >= size)
      throw new IllegalStateException(
        "Cannot write to an offset that hasn't been written to"
      )
    elems((readIdx.get() + offset) % capacity) = value
  }

  /**
   * Write a sequence of values to the buffer, modifying the state of the
   * buffer.
   * @param values
   *   The values to be written to the buffer
   */
  @varargs
  def write(values: T*): Unit = {
    values.foreach(write)
  }

  /**
   * @inheritdoc
   *
   * @note
   *   This method does not modify the underlying buffer state
   */
  def apply(i: Int): T = read(i)

  /**
   * @inheritdoc
   * @note
   *   This method does not modify the underlying buffer state
   */
  override def view: IndexedSeqView[T] = new IndexedSeqView[T] {
    def length: Int = self.count
    def apply(i: Int): T = self(i)
  }

  /** @inheritdoc */
  override def knownSize: Int = count

  /** @inheritdoc */
  override def className = "ConcurrentCircularBuffer"

  /** @inheritdoc */
  override val iterableFactory: IterableFactory[ConcurrentCircularBuffer] =
    new ConcurrentCircularBufferFactory(capacity)

  /**
   * This method does not modify this underlying [[ConcurrentCircularBuffer]],
   * but will return a copy with the new element appended.
   *
   * @param elem
   *   The element to append
   * @tparam B
   *   The type of [[ConcurrentCircularBuffer]] element
   * @return
   *   A deep copy of this buffer with a given element appended
   */
  @`inline` def :+[B >: T](elem: B): ConcurrentCircularBuffer[B] = appended(
    elem
  )

  private[this] def appended[B >: T](elem: B): ConcurrentCircularBuffer[B] = {
    val newElems = Array.ofDim[Any](capacity)
    Array.copy(elems, 0, newElems, 0, capacity)

    val cb = new ConcurrentCircularBuffer[Any](
      capacity,
      elems = newElems,
      readIdx = self.readIdx.get(),
      writeIdx = self.writeIdx.get()
    )
    val ret = cb.asInstanceOf[ConcurrentCircularBuffer[B]]
    ret.write(elem)
    ret
  }

  /**
   * @inheritdoc
   * @note
   *   This method does not modify the underlying buffer state
   */
  def iterator: Iterator[T] = view.iterator

}

private[timeseries] class ConcurrentCircularBufferFactory(capacity: Int)
    extends IterableFactory[ConcurrentCircularBuffer] {

  def from[A](source: IterableOnce[A]): ConcurrentCircularBuffer[A] =
    source match {
      case cb: ConcurrentCircularBuffer[A] if cb.capacity == capacity => cb
      case _ => (newBuilder[A] ++= source).result()
    }

  def empty[A]: ConcurrentCircularBuffer[A] =
    new ConcurrentCircularBuffer[Any](capacity)
      .asInstanceOf[ConcurrentCircularBuffer[A]]

  def newBuilder[A]: mutable.Builder[A, ConcurrentCircularBuffer[A]] =
    new mutable.ImmutableBuilder[A, ConcurrentCircularBuffer[A]](empty) {
      def addOne(elem: A): this.type = { elems = elems :+ elem; this }
    }

}