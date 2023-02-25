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
private[timeseries] class CircularBuffer[T: ClassTag](val capacity: Int)
    extends Iterable[T]
    with IterableOps[T, CircularBuffer, CircularBuffer[T]]
    with IterableFactoryDefaults[T, CircularBuffer]
    with StrictOptimizedIterableOps[T, CircularBuffer, CircularBuffer[T]]
    with IndexedSeqOps[T, CircularBuffer, CircularBuffer[T]] {

  private[this] val buffer: Array[T] = new Array[T](capacity)
  private[this] var readIdx: Int = 0
  private[this] var writeIdx: Int = 0
  private[this] var count = 0

  /** @return */
  def read(): T = {
    verifyOverflow(readIdx)
    val v = buffer(readIdx)
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
    buffer(idx)
  }

  /** @param value */
  def write(value: T): Unit = {
    buffer(writeIdx) = value
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

  private[this] def verifyOverflow(idx: Int): Unit =
    if (isEmpty)
      throw new IllegalStateException("Cannot read ahead of written value")

  private[this] def incr(idx: Int, amount: Int = 1): Int =
    Math.abs((idx + amount) % capacity)

  def apply(i: Int): T = read(i)

  override def view: IndexedSeqView[T] = new IndexedSeqView[T] {
    def length: Int = CircularBuffer.this.count
    def apply(i: Int): T = read(i)
  }

  override def knownSize: Int = count

  override def className = "CircularBuffer"

  override val iterableFactory: IterableFactory[CircularBuffer] =
    new CircularBufferFactory(capacity)

  override def length: Int = count

  override def appended[B >: T](elem: B): CircularBuffer[B] = {
    val cb = new CircularBuffer[Any](capacity).asInstanceOf[CircularBuffer[B]]
    this.foreach(cb.write)
    cb.write(elem)
    cb
  }
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
