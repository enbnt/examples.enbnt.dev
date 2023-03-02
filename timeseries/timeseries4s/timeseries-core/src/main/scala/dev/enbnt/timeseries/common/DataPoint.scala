package dev.enbnt.timeseries.common

import com.twitter.util.Time
import dev.enbnt.timeseries.common.Value._

private[timeseries] object DataPoint {
  implicit val timeOrdering: Ordering[DataPoint] = Ordering.by(_.time)
  implicit val valueOrdering: Ordering[DataPoint] = Ordering.by(_.value)
  implicit val asPair: DataPoint => (Time, Value) = dp => (dp.time, dp.value)

  def apply(time: Time, value: Float): DataPoint =
    DataPoint(time, value.asValue)
  def apply(time: Time, value: Double): DataPoint =
    DataPoint(time, value.asValue)
  def apply(time: Time, value: Int): DataPoint = DataPoint(time, value.asValue)
  def apply(time: Time, value: Long): DataPoint = DataPoint(time, value.asValue)

}

/**
 * A simple [[Time]] and [[Double value]] pairing.
 *
 * @param time
 *   The time at which the value was recorded
 * @param value
 *   The measured value of that data associated with the given [[time]]
 */
private[timeseries] final case class DataPoint(time: Time, value: Value)
