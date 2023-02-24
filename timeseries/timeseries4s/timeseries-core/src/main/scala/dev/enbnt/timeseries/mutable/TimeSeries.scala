package dev.enbnt.timeseries.mutable

import com.twitter.util.{Duration, Time}
import dev.enbnt.timeseries.{DataPoint, Seekable, TimeSeriesLike}
import dev.enbnt.timeseries.util.CircularBuffer

sealed trait TimeSeries extends TimeSeriesLike {
  def append(dataPoint: DataPoint): Unit
}

class CircularBufferTimeSeries(override val interval: Duration, maxSize: Int)
    extends TimeSeries
    with Seekable {

  private[this] val buffer: CircularBuffer[DataPoint] =
    new CircularBuffer[DataPoint](maxSize)

  override def append(dataPoint: DataPoint): Unit = buffer.write(dataPoint)

  override def startTime: Time =
    if (buffer.isEmpty) Time.Bottom else buffer.readRaw(buffer.readIndex()).time

  override def endTime: Time =
    if (buffer.isEmpty) Time.Top else buffer.readRaw(buffer.writeIndex()).time

  override def iterator: Iterator[DataPoint] = buffer.iterator

  override protected def timeIndex(time: Time): Int = ???
}
