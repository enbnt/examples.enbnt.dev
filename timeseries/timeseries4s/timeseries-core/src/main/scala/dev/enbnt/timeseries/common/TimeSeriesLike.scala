package dev.enbnt.timeseries.common

import com.twitter.util.Duration
import com.twitter.util.Time

/** A base trait for TimeSeries implementations. */
trait TimeSeriesLike extends Iterable[DataPoint] {

  def start: Time
  def end: Time
  def interval: Duration

  override def equals(obj: Any): Boolean = obj match {
    case ts: TimeSeriesLike =>
      this.eq(ts) ||
      (this.interval == ts.interval
        && this.start == ts.start
        && this.end == ts.end
        && iterator.sameElements(ts.iterator))
    case _ =>
      false
  }

}
