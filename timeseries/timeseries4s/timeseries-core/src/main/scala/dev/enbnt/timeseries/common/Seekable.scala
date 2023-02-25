package dev.enbnt.timeseries.common

import com.twitter.util.Time

trait Seekable extends TimeSeriesLike {

  def timeIndex(time: Time): Int

}
