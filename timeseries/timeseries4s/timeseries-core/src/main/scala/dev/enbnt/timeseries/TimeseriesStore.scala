package dev.enbnt.timeseries

import com.twitter.util.Time
import TimeseriesOps._

sealed trait TimeseriesStore {

  /** 
   * Retrieve [[Timeseries]] values between [[begin]] and [[end]] time
   * (inclusive)
   */
  def get(label: String, begin: Time, end: Time): Option[Timeseries]

}

class InMemoryTimeseriesStore(underlying: Map[String, Timeseries])
    extends TimeseriesStore
    with Map[String, Timeseries] {

  def this() = this(Map.empty)

  def iterator: Iterator[(String, Timeseries)] = underlying.iterator

  def removed(
      key: String
  ): Map[String, dev.enbnt.timeseries.Timeseries] =
    underlying.removed(key)

  def updated[V1 >: Timeseries](
      key: String,
      value: V1
  ): Map[String, V1] =
    underlying.updated(key, value)

  // Members declared in scala.collection.MapOps
  def get(key: String): Option[dev.enbnt.timeseries.Timeseries] =
    underlying.get(key)

  def get(label: String, begin: Time, end: Time): Option[Timeseries] =
    underlying.get(label) match {
      case Some(ts) =>
        Some(ts.range(begin, end))
      case _ =>
        None
    }

}
