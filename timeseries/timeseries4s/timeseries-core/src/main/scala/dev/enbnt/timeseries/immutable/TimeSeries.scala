package dev.enbnt.timeseries.immutable

import com.twitter.util.{Duration, Time}
import dev.enbnt.timeseries.common.{DataPoint, Seekable, TimeSeriesLike}

import scala.collection.{
  IndexedSeqOps,
  IndexedSeqView,
  IterableFactory,
  IterableFactoryDefaults,
  IterableOps,
  Searching,
  StrictOptimizedIterableOps,
  mutable
}

sealed trait TimeSeries extends TimeSeriesLike

object TimeSeries {
  def apply(interval: Duration, data: Iterable[DataPoint]): TimeSeries =
    data match {
      case e if e.isEmpty => EmptyTimeSeries
      case single if single.size == 1 =>
        new DenseTimeSeries(
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
          new SparseTimeSeries(
            interval,
            times.toIndexedSeq,
            values.toIndexedSeq
          )
        } else {
          new DenseTimeSeries(
            interval,
            data.head.time,
            data.map(_.value).toIndexedSeq
          )
        }
    }

  def empty(): TimeSeries = EmptyTimeSeries
}

case object EmptyTimeSeries
    extends TimeSeries
    with Seekable
    with IndexedSeqOps[DataPoint, Iterable, Iterable[DataPoint]] {
  def interval: Duration = Duration.Zero
  override def start: Time = Time.Bottom
  override def end: Time = Time.Top
  def apply(i: Int): DataPoint = throw new IndexOutOfBoundsException()
  override def toString(): String = "EmptyTimeSeries"
  override def length: Int = 0
  override def timeIndex(time: Time): Int = -1
}

final class SparseTimeSeries(
    val interval: Duration,
    times: IndexedSeq[Time],
    values: IndexedSeq[Double]
) extends TimeSeries
    with Seekable {

  require(times.nonEmpty)
  require(times.size == values.size)

  override def start: Time = times.head
  override def end: Time = times.last
  override def apply(i: Int): DataPoint = DataPoint(times(i), values(i))
  override def length: Int = values.length
  override def toString(): String = s"SparseTimeSeries[${super.toString()}]"
  override def timeIndex(time: Time): Int = {
    times.search(time) match {
      case Searching.Found(idx)                     => idx
      case Searching.InsertionPoint(idx) if idx > 0 => idx
      case _ => -1 // special case at 0 index
    }
  }

}

final class DenseTimeSeries(
    val interval: Duration,
    val start: Time,
    values: IndexedSeq[Double]
) extends TimeSeries
    with Seekable { self =>

  require(values.nonEmpty)

  private[this] def timeAt(index: Int): Time =
    start + (interval * index)

  override def apply(i: Int): DataPoint = DataPoint(timeAt(i), values(i))

  override def length: Int = values.length

  override def toString(): String = s"DenseTimeSeries[${super.toString()}]"

  override def timeIndex(time: Time): Int =
    ((time - start).inNanoseconds / interval.inNanoseconds).toInt

  override def end: Time = timeAt(values.length - 1)

  override def view: IndexedSeqView[DataPoint] = new IndexedSeqView[DataPoint] {
    def length: Int = self.length
    def apply(i: Int): DataPoint = self(i)
  }

  override def knownSize: Int = values.length

  override def className = "DenseTimeSeries"

}
