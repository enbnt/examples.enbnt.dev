package dev.enbnt.timeseries.util

import scala.annotation.varargs
import scala.collection.AbstractIterator
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
private[timeseries] class CircularBuffer[T: ClassTag](maxSize: Int)
    extends Iterable[T] {

  private[this] val buffer: Array[T] = new Array[T](maxSize)
  private[this] var readIdx: Int = 0
  private[this] var writeIdx: Int = 0
  private[this] var count = 0

  override def isEmpty: Boolean = count == 0
  override def size: Int = count

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
//    verifyOverflow(idx)
    buffer(idx)
  }

  /** @param value */
  def write(value: T): Unit = {
    buffer(writeIdx) = value
    writeIdx = incr(writeIdx)
    count += 1
    if (count > maxSize) {
      readIdx = incr(writeIdx)
      count -= 1
    }
  }

  @varargs
  def write(values: T*): Unit = {
    values.foreach(write)
  }

  def writeIndex(offset: Int = 0): Int = incr(writeIdx, offset)
  def readIndex(offset: Int = 0): Int = {
    val idx = incr(readIdx, offset)
    verifyOverflow(idx)
    idx
  }
  def readRaw(idx: Int): T = {
    verifyOverflow(idx)
    buffer(idx)
  }

  private[this] def verifyOverflow(idx: Int): Unit =
    if (isEmpty)
      throw new IllegalStateException("Cannot read ahead of written value")

  private[this] def incr(idx: Int, amount: Int = 1): Int =
    Math.abs((idx + amount) % maxSize)

  override def iterator: Iterator[T] =
    if (isEmpty) Iterator.empty[T]
    else
      new AbstractIterator[T] {
        override def hasNext: Boolean = nonEmpty
        override def next(): T = read()
      }
}
