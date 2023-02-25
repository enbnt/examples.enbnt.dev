package dev.enbnt.timeseries.common

import com.twitter.util.Time

import scala.collection.IndexedSeqOps

trait Seekable extends TimeSeriesLike {

  def timeIndex(time: Time): Int

}
