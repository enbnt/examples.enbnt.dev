package dev.enbnt.timeseries

import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike
import dev.enbnt.timeseries.common.Value
import scala.collection.Searching

object TimeSeriesOps {
  implicit final class RichTimeSeries(val ts: TimeSeriesLike)
      extends scala.AnyVal {

    def range(begin: Time, end: Time): TimeSeriesLike = {
      require(begin <= end)
      if (begin <= ts.start && end >= ts.end) ts
      else {
        val underlying: Iterable[DataPoint] = ts match {
          case seek: Seekable =>
            val startIdx: Int = idxFor(seek.indexAt(begin))
            val endIdx: Int = seek.size.min(idxFor(seek.indexAt(end)) + 1)
            ts.slice(startIdx, endIdx)
          case _ =>
            ts.dropWhile(_.time < begin).takeWhile(_.time <= end)
        }

        if (ts.isEmpty) TimeSeries.empty()
        else
          TimeSeries(ts.interval, underlying)
      }

    }

    def get(time: Time): Option[DataPoint] = {
      ts match {
        case seek: Seekable =>
          seek.indexAt(time) match {
            case Searching.Found(idx) =>
              val dp = seek(idx)
              if (dp.value == Value.Undefined) None else Some(dp)
            case _ => None
          }
        case _ =>
          ts.find(_.time == time)
      }
    }

    private[this] def idxFor(searching: Searching.SearchResult): Int =
      searching match {
        case Searching.Found(idx)                     => idx
        case Searching.InsertionPoint(idx) if idx > 0 => idx
        case _ => -1 // special case at 0 index
      }
  }

}
