package dev.enbnt.timeseries.mutable

import com.twitter.util.{Duration, Time}
import dev.enbnt.timeseries.{DataPoint, Seekable, TimeSeriesLike}
import dev.enbnt.timeseries.util.CircularBuffer

sealed trait TimeSeries extends TimeSeriesLike {
  def append(dataPoint: DataPoint): Unit
}

class CircularBufferTimeSeries(override val interval: Duration, maxSize: Int) extends TimeSeries with Seekable {

  private[this] val buffer: CircularBuffer[DataPoint] = new CircularBuffer[DataPoint](maxSize)

  override def append(dataPoint: DataPoint): Unit = buffer.write(dataPoint)

  override def startTime: Time = buffer.readRaw(buffer.writeIndex(maxSize)).time

  override def endTime: Time = buffer.readRaw(buffer.writeIndex()).time

  override def iterator: Iterator[DataPoint] = new Iterator[DataPoint] {
    override def hasNext: Boolean = buffer.readIndex() != buffer.writeIndex()
    override def next(): DataPoint = buffer.read()
  }

  override protected def timeIndex(time: Time): Int = ???
}