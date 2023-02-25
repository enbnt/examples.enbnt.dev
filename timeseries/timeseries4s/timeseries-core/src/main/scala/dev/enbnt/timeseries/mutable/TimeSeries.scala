package dev.enbnt.timeseries.mutable

import com.twitter.util.{Duration, Time}
import dev.enbnt.timeseries.common.{DataPoint, Seekable, TimeSeriesLike}
import dev.enbnt.timeseries.util.CircularBuffer

import scala.collection.Searching

sealed trait TimeSeries extends TimeSeriesLike {
  def append(dataPoint: DataPoint): Unit
}

class CircularBufferTimeSeries(override val interval: Duration, capacity: Int)
    extends CircularBuffer[DataPoint](capacity)
    with TimeSeries
    with Seekable {

  private[this] val buffer: CircularBuffer[DataPoint] =
    new CircularBuffer[DataPoint](capacity)

  override def append(dataPoint: DataPoint): Unit = buffer.write(dataPoint)

  override def start: Time = buffer.headOption match {
    case Some(dp) => dp.time
    case _        => Time.Top
  }

  override def end: Time = buffer.headOption match {
    case Some(dp) => dp.time
    case _        => Time.Bottom
  }

  override def timeIndex(time: Time): Int =
    buffer.search(DataPoint(time, 0))(DataPoint.timeOrdering) match {
      case Searching.Found(idx)                     => idx
      case Searching.InsertionPoint(idx) if idx > 0 => idx
      case _                                        => -1
    }
}
