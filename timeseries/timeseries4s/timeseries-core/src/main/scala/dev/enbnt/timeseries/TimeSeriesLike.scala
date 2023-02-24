package dev.enbnt.timeseries

import com.twitter.util.{Duration, Time}

trait TimeSeriesLike extends Iterable[DataPoint] {
  def startTime: Time
  def endTime: Time
  def interval: Duration

}
