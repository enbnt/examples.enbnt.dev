package dev.enbnt.timeseries

import com.twitter.util.Time

trait Seekable { self: TimeSeriesLike =>

  def timeIndex(time: Time): Int

}
