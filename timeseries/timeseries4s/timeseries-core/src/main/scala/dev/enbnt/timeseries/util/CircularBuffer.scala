package dev.enbnt.timeseries.util

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
 *     val ring = new CircularBuffer[String](2)  // ring: [_, _] read: 0 write: 0 size: 0
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
 *   This implementation is <b>NOT</b> thread safe.
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
 * @param count
 *   The number of elements present within the buffer.
 * @tparam T
 *   The type of elements of contained within the buffer
 */
private[timeseries] class CircularBuffer[T: ClassTag] private (
  val capacity: Int,
  elems: Array[T],
  var readIdx: Int,
  var writeIdx: Int,
  var count: Int
) extends Iterable[T]
    with IterableOps[T, CircularBuffer, CircularBuffer[T]]
    with IterableFactoryDefaults[T, CircularBuffer]
    with StrictOptimizedIterableOps[T, CircularBuffer, CircularBuffer[T]] {
  self =>

  require(
    capacity > 0,
    s"CircularBuffer capacity must be > 0, but received '$capacity'"
  )

  def this(capacity: Int) = this(
    capacity,
    elems = Array.ofDim[T](capacity),
    readIdx = 0,
    writeIdx = 0,
    count = 0
  )

  /**
   * Consume the value at the current read index, modifying the state of the
   * buffer. An [[IllegalStateException]] will be thrown if the buffer is empty
   * when a read is attempted.
   */
  @throws[IllegalStateException]
  def read(): T = {
    if (isEmpty)
      throw new IllegalStateException("Cannot read ahead of written value")
    val v = elems(readIdx)
    readIdx = incr(readIdx)
    count -= 1
    v
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
    val idx = incr(readIdx, offset)
    elems(idx)
  }

  /**
   * Set the value at the current write index, modifying the state of the
   * buffer.
   */
  def write(value: T): Unit = {
    elems(writeIdx) = value
    writeIdx = incr(writeIdx)
    count += 1
    if (count > capacity) {
      readIdx = incr(writeIdx)
      count -= 1
    }
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

  // Increment an index by a specified amount, accounting for loops in the buffer space
  private[this] def incr(idx: Int, amount: Int = 1): Int =
    Math.abs((idx + amount) % capacity)

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
  override def className = "CircularBuffer"

  /** @inheritdoc */
  override val iterableFactory: IterableFactory[CircularBuffer] =
    new CircularBufferFactory(capacity)

  /**
   * This method does not modify this underlying [[CircularBuffer]], but will
   * return a copy with the new element appended.
   *
   * @param elem
   *   The element to append
   * @tparam B
   *   The type of [[CircularBuffer]] element
   * @return
   *   A deep copy of this buffer with a given element appended
   */
  @`inline` def :+[B >: T](elem: B): CircularBuffer[B] = appended(elem)

  private[this] def appended[B >: T](elem: B): CircularBuffer[B] = {
    val newElems = Array.ofDim[Any](capacity)
    Array.copy(elems, 0, newElems, 0, capacity)

    val cb = new CircularBuffer[Any](
      capacity,
      elems = newElems,
      readIdx = self.readIdx,
      writeIdx = self.writeIdx,
      count = count
    )
    val ret = cb.asInstanceOf[CircularBuffer[B]]
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

private[timeseries] class CircularBufferFactory(capacity: Int)
    extends IterableFactory[CircularBuffer] {

  def from[A](source: IterableOnce[A]): CircularBuffer[A] =
    source match {
      case cb: CircularBuffer[A] if cb.capacity == capacity => cb
      case _ => (newBuilder[A] ++= source).result()
    }

  def empty[A]: CircularBuffer[A] =
    new CircularBuffer[Any](capacity).asInstanceOf[CircularBuffer[A]]

  def newBuilder[A]: mutable.Builder[A, CircularBuffer[A]] =
    new mutable.ImmutableBuilder[A, CircularBuffer[A]](empty) {
      def addOne(elem: A): this.type = { elems = elems :+ elem; this }
    }

}
