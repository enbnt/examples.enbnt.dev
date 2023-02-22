package dev.enbnt.timeseries

import com.twitter.util.Duration
import com.twitter.util.Time

import scala.collection.Searching

object Timeseries {

  def apply(interval: Duration, data: IndexedSeq[DataPoint]): Timeseries =
    data match {
      case e if e.isEmpty => EmptyTimeseries
      case single if single.size == 1 =>
        new DenseTimeseries(
          interval,
          single.head.time,
          IndexedSeq(single.head.value)
        )
      case _ =>
        val size = data.size
        val span: Int =
          ((data.last.time - data.head.time).inNanoseconds / interval.inNanoseconds).toInt
        if (span < size / 2) {
          val (times, values) = data.unzip
          new SparseTimeseries(interval, times, values)
        } else {
          new DenseTimeseries(interval, data.head.time, data.map(_.value))
        }
    }

}

sealed trait Timeseries extends IndexedSeq[DataPoint] {
  def start: Time
  def end: Time
  def interval: Duration

  private[timeseries] def timeIndex(time: Time): Int

  override def equals(obj: Any): Boolean = obj match {
    case ts: Timeseries =>
      interval == ts.interval && this.sameElements(ts)
    case _ =>
      false
  }
}

case object EmptyTimeseries extends Timeseries {
  def apply(i: Int): DataPoint = throw new IndexOutOfBoundsException()
  def start: Time = Time.Bottom
  def end: Time = Time.Top
  def interval: Duration = Duration.Zero
  override def toString(): String = "EmptyTimeseries"
  override def length: Int = 0
  override private[timeseries] def timeIndex(time: Time): Int = -1
}

private[timeseries] final class SparseTimeseries(
    val interval: Duration,
    times: IndexedSeq[Time],
    values: IndexedSeq[Double]
) extends Timeseries {
  def start: Time = times.head
  def end: Time = times.last
  override def apply(i: Int): DataPoint = DataPoint(times(i), values(i))
  override def length: Int = values.length
  override def toString(): String = s"Sparse[${super.toString()}]"
  override private[timeseries] def timeIndex(time: Time): Int = {
    times.search(time) match {
      case Searching.Found(idx)                     => idx
      case Searching.InsertionPoint(idx) if idx > 0 => idx
      case _ => -1 // special case at 0 index
    }
  }
}

private[timeseries] final class DenseTimeseries(
    val interval: Duration,
    val start: Time,
    values: IndexedSeq[Double]
) extends Timeseries {

  def end: Time = timeAt(values.length - 1)

  private[this] def timeAt(index: Int): Time =
    start + (interval * index)

  override def apply(i: Int): DataPoint = DataPoint(timeAt(i), values(i))

  override def length: Int = values.length

  override def toString(): String = s"Dense[${super.toString()}]"

  override private[timeseries] def timeIndex(time: Time): Int =
    ((time - start).inNanoseconds / interval.inNanoseconds).toInt
}
