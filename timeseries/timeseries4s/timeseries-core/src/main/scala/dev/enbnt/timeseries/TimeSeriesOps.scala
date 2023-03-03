package dev.enbnt.timeseries

import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.DataPointOps._
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike
import dev.enbnt.timeseries.common.Value
import dev.enbnt.timeseries.common.Value._
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

    // TODO - lots and lots of scaladoc
    // note - All TimeSeriesLike -> TimeSeriesLike operations will only
    //        result in values at times that are defined in `ts` and also
    //        exist in `other`

    def +(other: TimeSeriesLike): TimeSeriesLike = applyOpTs(other)(_ + _)
    def +(v: Value): TimeSeriesLike = applyOpValue(v)(_ + _)
    def +(v: Float): TimeSeriesLike = ts + v.asValue
    def +(v: Double): TimeSeriesLike = ts + v.asValue
    def +(v: Int): TimeSeriesLike = ts + v.asValue
    def +(v: Long): TimeSeriesLike = ts + v.asValue
    def -(other: TimeSeriesLike): TimeSeriesLike = applyOpTs(other)(_ - _)
    def -(v: Value): TimeSeriesLike = applyOpValue(v)(_ - _)
    def -(v: Float): TimeSeriesLike = ts - v.asValue
    def -(v: Double): TimeSeriesLike = ts - v.asValue
    def -(v: Int): TimeSeriesLike = ts - v.asValue
    def -(v: Long): TimeSeriesLike = ts - v.asValue
    def *(other: TimeSeriesLike): TimeSeriesLike = applyOpTs(other)(_ * _)
    def *(v: Value): TimeSeriesLike = applyOpValue(v)(_ * _)
    def *(v: Float): TimeSeriesLike = ts * v.asValue
    def *(v: Double): TimeSeriesLike = ts * v.asValue
    def *(v: Int): TimeSeriesLike = ts * v.asValue
    def *(v: Long): TimeSeriesLike = ts * v.asValue
    def /(other: TimeSeriesLike): TimeSeriesLike = applyOpTs(other)(_ / _)
    def /(v: Value): TimeSeriesLike = applyOpValue(v)(_ / _)
    def /(v: Float): TimeSeriesLike = ts / v.asValue
    def /(v: Double): TimeSeriesLike = ts / v.asValue
    def /(v: Int): TimeSeriesLike = ts / v.asValue
    def /(v: Long): TimeSeriesLike = ts / v.asValue

    private[this] def applyOpTs(
      ts2: TimeSeriesLike
    )(f: (DataPoint, DataPoint) => DataPoint): TimeSeriesLike = {
      require(
        ts.interval == ts2.interval,
        "TimeSeries with unmatched intervals cannot be added"
      )
      if (ts.isEmpty || ts2.isEmpty || ts.end < ts2.start || ts2.end < ts.start)
        TimeSeries.empty()
      else {
        val data: Iterable[DataPoint] = ts.flatMap { dp =>
          ts2.get(dp.time) match {
            case Some(dp2) => Some(f(dp, dp2))
            case _         => None
          }
        }
        TimeSeries(ts.interval, data)
      }
    }

    private[this] def applyOpValue(
      v: Value
    )(f: (DataPoint, Value) => DataPoint): TimeSeriesLike = {
      if (ts.isEmpty || v == Value.Undefined) TimeSeries.empty()
      else {
        val data: Iterable[DataPoint] = ts.map(f(_, v))
        TimeSeries(ts.interval, data)
      }
    }
  }

}
