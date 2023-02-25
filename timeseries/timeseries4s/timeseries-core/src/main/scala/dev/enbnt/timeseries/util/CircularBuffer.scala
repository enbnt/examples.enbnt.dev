package dev.enbnt.timeseries.util

import scala.annotation.varargs
import scala.collection.{
  IndexedSeqOps,
  IndexedSeqView,
  IterableFactory,
  IterableFactoryDefaults,
  IterableOps,
  StrictOptimizedIterableOps,
  mutable
}
import scala.reflect.ClassTag

/**
 * @param size
 * @param classTag$T$0
 * @tparam T
 *
 * @see
 *   [[https://en.wikipedia.org/wiki/Circular_buffer]]
 * @note
 *   This implementation is <b>NOT</b> thread safe.
 */
private[timeseries] class CircularBuffer[T: ClassTag](
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

  def this(capacity: Int) = this(
    capacity,
    elems = Array.ofDim[T](capacity),
    readIdx = 0,
    writeIdx = 0,
    count = 0
  )

  /** @return */
  def read(): T = {
    verifyOverflow()
    val v = elems(readIdx)
    readIdx = incr(readIdx)
    count -= 1
    v
  }

  /**
   * Reads the value at the offset of the current read index position,
   * accounting for loops in the buffer.
   *
   * @note
   *   Calling this method does not modify the read index position.
   * @param offset
   * @return
   */
  def read(offset: Int): T = {
    if (offset >= size)
      throw new IllegalStateException(
        "Cannot read beyond the size of this buffer"
      )
    val idx = incr(readIdx, offset)
    elems(idx)
  }

  /** @param value */
  def write(value: T): Unit = {
    elems(writeIdx) = value
    writeIdx = incr(writeIdx)
    count += 1
    if (count > capacity) {
      readIdx = incr(writeIdx)
      count -= 1
    }
  }

  @varargs
  def write(values: T*): Unit = {
    values.foreach(write)
  }

  private[this] def verifyOverflow(): Unit =
    if (isEmpty)
      throw new IllegalStateException("Cannot read ahead of written value")

  private[this] def incr(idx: Int, amount: Int = 1): Int =
    Math.abs((idx + amount) % capacity)

  def apply(i: Int): T = read(i)

  override def view: IndexedSeqView[T] = new IndexedSeqView[T] {
    def length: Int = self.count
    def apply(i: Int): T = self(i)
  }

  override def knownSize: Int = count

  override def className = "CircularBuffer"

  override val iterableFactory: IterableFactory[CircularBuffer] =
    new CircularBufferFactory(capacity)

  @`inline` def :+[B >: T](elem: B): CircularBuffer[B] = appended(elem)

  def appended[B >: T](elem: B): CircularBuffer[B] = {
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

  def iterator: Iterator[T] = view.iterator

}

class CircularBufferFactory(capacity: Int)
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
