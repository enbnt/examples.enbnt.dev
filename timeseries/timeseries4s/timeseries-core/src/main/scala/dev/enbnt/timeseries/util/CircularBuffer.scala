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
private[timeseries] class CircularBuffer[T: ClassTag](size: Int)
    extends Iterable[T] {

  private[this] val buffer: Array[T] = new Array[T](size)
  private[this] var readIdx: Int = 0
  private[this] var writeIdx: Int = 0
  private[this] var overflow: Boolean = false

  /** @return */
  def read(): T = {
    verifyOverflow(readIdx)
    val v = buffer(readIdx)
    readIdx = incr(readIdx)
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
    val idx = incr(readIdx, offset)
    verifyOverflow(idx)
    buffer(idx)
  }

  /** @param value */
  def write(value: T): Unit = {
    buffer(writeIdx) = value
    writeIdx = incr(writeIdx)
    if (!overflow && writeIdx == 0) overflow = true
    if (overflow && writeIdx == readIdx) readIdx = incr(readIdx)
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
    if (!overflow && idx >= writeIdx)
      throw new IllegalStateException("Cannot read ahead of written value")

  private[this] def incr(idx: Int, amount: Int = 1): Int =
    Math.abs((idx + amount) % size)

  override def iterator: Iterator[T] =
    if (!overflow && readIdx == writeIdx) Iterator.empty[T]
    else
      new AbstractIterator[T] {
        override def hasNext: Boolean = !overflow && readIdx != writeIdx
        override def next(): T = read()
      }
}
