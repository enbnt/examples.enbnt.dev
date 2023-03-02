package dev.enbnt.timeseries.immutable

import com.twitter.util.Duration
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike
import dev.enbnt.timeseries.common.Value
import scala.collection.IndexedSeqOps
import scala.collection.IndexedSeqView
import scala.collection.Searching
import scala.collection.Searching.SearchResult

sealed trait TimeSeries extends TimeSeriesLike

object TimeSeries {
  def apply(interval: Duration, data: Iterable[DataPoint]): TimeSeries =
    data match {
      case e if e.isEmpty => EmptyTimeSeries
      case single if single.size == 1 =>
        val dp = single.head
        if (dp.value == Value.Undefined) EmptyTimeSeries
        else DenseTimeSeries(interval, single)

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
          ((end - start).inNanoseconds / interval.inNanoseconds).toInt + 1

        if (span < size / 2) {
          SparseTimeSeries(interval, data)
        } else {
          DenseTimeSeries(interval, data)
        }

    }

  def empty(): TimeSeries = EmptyTimeSeries
}

/** Represents a [[TimeSeries]] with no values and an undefined [[interval]]. */
private[timeseries] case object EmptyTimeSeries
    extends TimeSeries
    with Seekable
    with IndexedSeqOps[DataPoint, Iterable, Iterable[DataPoint]] {

  private[this] val NotFound: SearchResult = Searching.InsertionPoint(0)

  def interval: Duration = Duration.Undefined
  override def start: Time = Time.Bottom
  override def end: Time = Time.Top
  def apply(i: Int): DataPoint = throw new IndexOutOfBoundsException()
  override def toString(): String = "EmptyTimeSeries"
  override def length: Int = 0
  override def indexAt(time: Time): SearchResult = NotFound
}

object SparseTimeSeries {
  def apply(interval: Duration, data: Iterable[DataPoint]): SparseTimeSeries = {
    val (times, values) = data.filterNot(_.value == Value.Undefined).unzip
    require(times.nonEmpty)
    new SparseTimeSeries(interval, times.toIndexedSeq, values.toIndexedSeq)
  }
}

/**
 * Represents a [[TimeSeries]] that is sparsely populated along its given
 * [[interval]]. In other words, if there are few existing entries spanning many
 * intervals, as [[SparseTimeSeries]] may be an appropriate representation.
 *
 * @param interval
 * @param times
 * @param values
 */
private[timeseries] final class SparseTimeSeries private (
  val interval: Duration,
  times: IndexedSeq[Time],
  values: IndexedSeq[Value]
) extends TimeSeries
    with Seekable
    with IndexedSeq[DataPoint] { self =>

  override def start: Time = times.head
  override def end: Time = times.last
  override def apply(i: Int): DataPoint = DataPoint(times(i), values(i))
  override def length: Int = values.length
  override def indexAt(time: Time): SearchResult = times.search(time)

  override def view: IndexedSeqView[DataPoint] = new IndexedSeqView[DataPoint] {
    def length: Int = self.length
    def apply(i: Int): DataPoint = self(i)
  }

  override def knownSize: Int = times.length

  override def className = "SparseTimeSeries"

}

object DenseTimeSeries {

  def apply(
    interval: Duration,
    iterable: Iterable[DataPoint]
  ): DenseTimeSeries = {

    // trim undefined values from the start and end of the iterable
    val trimmed = iterable.filterNot(_.value == Value.Undefined)
    require(trimmed.nonEmpty, "DenseTimeSeries must be non-empty")

    val start = trimmed.head.time
    val end = trimmed.last.time

    val size: Int =
      ((end - start).inNanoseconds / interval.inNanoseconds).toInt + 1

    val ar = Array.fill[Value](size)(Value.Undefined)

    var definedCount = 0
    trimmed.foreach { dp =>
      if (dp.value != Value.Undefined) {
        val idx =
          (start.until(dp.time).inNanoseconds / interval.inNanoseconds).toInt
        ar(idx) = dp.value
        definedCount += 1
      }
    }

    new DenseTimeSeries(interval, start, ar, size - definedCount)
  }

}

private[timeseries] final class DenseTimeSeries private (
  val interval: Duration,
  val start: Time,
  values: IndexedSeq[Value],
  undefinedCount: Int
) extends TimeSeries
    with Seekable
    with IndexedSeq[DataPoint] { self =>

  override def apply(i: Int): DataPoint = DataPoint(timeAt(i), values(i))

  override def length: Int = values.size - undefinedCount

  /**
   * Retrieve the index which corresponds to the associated [[Time time]].
   *
   * @param time
   *   The time to search for.
   * @return
   *   The index associated with the given time, if it is found.
   */
  override def indexAt(time: Time): SearchResult = {
    time match {
      case t if t < start => Searching.InsertionPoint(0)
      case t if t > end   => Searching.InsertionPoint(size)
      case _ =>
        val idx = ((time - start).inNanoseconds / interval.inNanoseconds).toInt
        Searching.Found(idx)
    }

  }

  override val end: Time = timeAt(values.length - 1)

  private[this] def timeAt(index: Int): Time =
    start + (interval * index)

  override lazy val view: IndexedSeqView[DataPoint] = {
    val data = values.zipWithIndex.collect {
      case (value, idx) if value != Value.Undefined =>
        DataPoint(timeAt(idx), value)
    }.toIndexedSeq

    new IndexedSeqView[DataPoint] {
      def length: Int = data.length
      def apply(i: Int): DataPoint = data(i)
    }
  }

}
