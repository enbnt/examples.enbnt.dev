package dev.enbnt.timeseries

import com.twitter.util.Time

object TimeseriesOps {
  implicit final class RichTimeseries(val ts: TimeSeriesLike) extends scala.AnyVal {

    def range(begin: Time, end: Time): Timeseries = {
      require(begin <= end)
      if (begin <= ts.startTime && end >= ts.endTime) ts
      else {
        val underlying = ts match {
          case seek: Seekable =>
            val b0 = seek.timeIndex(begin)
            val e0 = ts.length.min(ts.timeIndex(end) + 1)

            val ts0 = ts.slice(b0, e0)
          case _ =>
            ts.dropWhile(_.time < begin).takeWhile(_.time <= end)
        }



        Timeseries(ts.interval, underlying)
      }


    }
  }

}
