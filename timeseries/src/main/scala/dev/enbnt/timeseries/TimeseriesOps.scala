package dev.enbnt.timeseries

import com.twitter.util.Time

import scala.collection.Searching

object TimeseriesOps {
  implicit final class RichTimeseries(val ts: Timeseries) extends scala.AnyVal {

    def range(begin: Time, end: Time): Timeseries = {
      val b0 = ts.timeIndex(begin)
      val e0 = ts.length.min(ts.timeIndex(end) + 1)

      val ts0 = ts.slice(b0, e0)

      Timeseries(ts.interval, ts0)
    }
  }

}
