package dev.enbnt.timeseries.immutable

import com.twitter.util.Duration
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike
import scala.collection.IndexedSeqOps
import scala.collection.IndexedSeqView
import scala.collection.IterableFactory
import scala.collection.IterableFactoryDefaults
import scala.collection.IterableOps
import scala.collection.Searching
import scala.collection.StrictOptimizedIterableOps
import scala.collection.mutable

sealed trait TimeSeries extends TimeSeriesLike

object TimeSeries {
  def apply(interval: Duration, data: Iterable[DataPoint]): TimeSeries =
    data match {
      case e if e.isEmpty => EmptyTimeSeries
      case single if single.size == 1 =>
        single.headOption match {
          case Some(dp) =>
            new DenseTimeSeries(interval, dp.time, IndexedSeq(dp.value))
          case _ =>
            throw new IllegalStateException(
              s"Expected a valid time stamp but found $data"
            )
        }

      case _ =>
        val size = data.size

        val end = data.lastOption match {
          case Some(dp) => dp.time
          case _ =>
            throw new IllegalStateException(
              s"Expected a valid end time but found $data"
            )
        }

        val start = data.headOption match {
          case Some(dp) => dp.time
          case _ =>
            throw new IllegalStateException(
              s"Expected a valid start time but found $data"
            )
        }

        val span: Int =
          ((end - start).inNanoseconds / interval.inNanoseconds).toInt
        if (span < size / 2) {
          val (times, values) = data.unzip
          new SparseTimeSeries(
            interval,
            times.toIndexedSeq,
            values.toIndexedSeq
          )
        } else {
          new DenseTimeSeries(interval, start, data.map(_.value).toIndexedSeq)
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
    with Seekable
    with IndexedSeq[DataPoint] { self =>

  require(times.nonEmpty)
  require(times.size == values.size)

  override def start: Time = times.head
  override def end: Time = times.last
  override def apply(i: Int): DataPoint = DataPoint(times(i), values(i))
  override def length: Int = values.length
  override def timeIndex(time: Time): Int = {
    times.search(time) match {
      case Searching.Found(idx)                     => idx
      case Searching.InsertionPoint(idx) if idx > 0 => idx
      case _ => -1 // special case at 0 index
    }
  }

  override def view: IndexedSeqView[DataPoint] = new IndexedSeqView[DataPoint] {
    def length: Int = self.length
    def apply(i: Int): DataPoint = self(i)
  }

  override def knownSize: Int = times.length

  override def className = "SparseTimeSeries"

}

final class DenseTimeSeries(
  val interval: Duration,
  val start: Time,
  values: IndexedSeq[Double]
) extends TimeSeries
    with Seekable
    with IndexedSeq[DataPoint] { self =>

  require(values.nonEmpty)

  private[this] def timeAt(index: Int): Time =
    start + (interval * index)

  override def apply(i: Int): DataPoint = DataPoint(timeAt(i), values(i))

  override def length: Int = values.length

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
