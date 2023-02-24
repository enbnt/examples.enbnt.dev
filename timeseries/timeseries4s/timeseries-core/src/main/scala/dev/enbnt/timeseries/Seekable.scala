package dev.enbnt.timeseries

import com.twitter.util.Time

trait Seekable { self: TimeSeriesLike =>

  protected def timeIndex(time: Time): Int

}
