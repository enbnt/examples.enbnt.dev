package dev.enbnt.timeseries

import com.twitter.util.Time
import TimeSeriesOps._

sealed trait TimeSeriesStore {

  /**
   * Retrieve [[TimeSeries]] values between [[begin]] and [[end]] time
   * (inclusive)
   */
  def get(label: String, begin: Time, end: Time): Option[TimeSeries]

}

class InMemoryTimeSeriesStore(underlying: Map[String, TimeSeries])
    extends TimeSeriesStore
    with Map[String, TimeSeries] {

  def this() = this(Map.empty)

  def iterator: Iterator[(String, TimeSeries)] = underlying.iterator

  def removed(key: String): Map[String, dev.enbnt.timeseries.TimeSeries] =
    underlying.removed(key)

  def updated[V1 >: TimeSeries](key: String, value: V1): Map[String, V1] =
    underlying.updated(key, value)

  // Members declared in scala.collection.MapOps
  def get(key: String): Option[dev.enbnt.timeseries.TimeSeries] =
    underlying.get(key)

  def get(label: String, begin: Time, end: Time): Option[TimeSeries] =
    underlying.get(label) match {
      case Some(ts) =>
        Some(ts.range(begin, end))
      case _ =>
        None
    }

}
