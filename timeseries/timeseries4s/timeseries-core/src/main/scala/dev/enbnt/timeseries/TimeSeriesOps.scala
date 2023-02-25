package dev.enbnt.timeseries

import com.twitter.util.Time
import dev.enbnt.timeseries.common.{Seekable, TimeSeriesLike}

object TimeSeriesOps {
  implicit final class RichTimeSeries(val ts: TimeSeriesLike)
      extends scala.AnyVal {

    def range(begin: Time, end: Time): TimeSeriesLike = {
      require(begin <= end)
      if (begin <= ts.start && end >= ts.end) ts
      else {
        val underlying = ts match {
          case seek: Seekable =>
            val b0 = seek.timeIndex(begin)
            val e0 = seek.knownSize.min(seek.timeIndex(end) + 1)

            ts.slice(b0, e0)
          case _ =>
            ts.dropWhile(_.time < begin).takeWhile(_.time <= end)
        }

        TimeSeries(ts.interval, underlying.toIndexedSeq)
      }

    }
  }

}
