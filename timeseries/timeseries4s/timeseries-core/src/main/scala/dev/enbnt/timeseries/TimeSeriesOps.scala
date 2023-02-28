package dev.enbnt.timeseries

import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike

object TimeSeriesOps {
  implicit final class RichTimeSeries(val ts: TimeSeriesLike)
      extends scala.AnyVal {

    def range(begin: Time, end: Time): TimeSeriesLike = {
      require(begin <= end)
      if (begin <= ts.start && end >= ts.end) ts
      else {
        val underlying: Iterable[DataPoint] = ts match {
          case seek: Seekable =>
            val startIdx = seek.indexAt(begin)
            val endIdx = seek.size.min(seek.indexAt(end) + 1)

            ts.view.slice(startIdx, endIdx)
          case _ =>
            ts.dropWhile(_.time < begin).takeWhile(_.time <= end)
        }

        if (ts.isEmpty) TimeSeries.empty()
        else TimeSeries(ts.interval, underlying)
      }

    }
  }

}
