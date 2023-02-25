package dev.enbnt.timeseries.common

import com.twitter.util.Time

import scala.collection.IndexedSeqOps

trait Seekable
    extends TimeSeriesLike
    with IndexedSeqOps[DataPoint, Iterable, Iterable[DataPoint]] {

  def timeIndex(time: Time): Int

}
