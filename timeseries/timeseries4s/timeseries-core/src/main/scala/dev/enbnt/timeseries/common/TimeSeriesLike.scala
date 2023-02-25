package dev.enbnt.timeseries.common

import com.twitter.util.{Duration, Time}

trait TimeSeriesLike extends Iterable[DataPoint] {
  def start: Time
  def end: Time
  def interval: Duration

  override def equals(obj: Any): Boolean = obj match {
    case ts: TimeSeriesLike =>
      this.interval == ts.interval && this.start == ts.start && this.end == ts.end && this.iterator
        .sameElements(ts)
    case _ => false
  }

}
