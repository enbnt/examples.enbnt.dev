package dev.enbnt.timeseries.mutable

import com.twitter.util.Duration
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.Seekable
import dev.enbnt.timeseries.common.TimeSeriesLike
import dev.enbnt.timeseries.util.CircularBuffer
import scala.collection.Searching

sealed trait TimeSeries extends TimeSeriesLike {
  def append(dataPoint: DataPoint): Unit

}

class CircularBufferTimeSeries(override val interval: Duration, capacity: Int)
    extends CircularBuffer[DataPoint](capacity)
    with TimeSeries
    with Seekable { self =>

  override def append(dataPoint: DataPoint): Unit = write(dataPoint)

  override def start: Time = self.headOption match {
    case Some(dp) => dp.time
    case _        => throw new IllegalStateException(s"No start in $this")
  }

  override def end: Time = lastOption match {
    case Some(dp) => dp.time
    case _        => throw new IllegalStateException(s"No end in $this")
  }

  override def indexAt(time: Time): Int = {
    this.toIndexedSeq.search(DataPoint(time, 0))(DataPoint.timeOrdering) match {
      case Searching.Found(idx)                     => idx
      case Searching.InsertionPoint(idx) if idx > 0 => idx
      case _ => -1 // special case at 0 index
    }
  }
}
