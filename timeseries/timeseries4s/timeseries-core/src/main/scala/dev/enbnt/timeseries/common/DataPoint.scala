package dev.enbnt.timeseries.common

import com.twitter.util.Time

private[timeseries] object DataPoint {
  implicit val timeOrdering: Ordering[DataPoint] = Ordering.by(_.time)
  implicit val valueOrdering: Ordering[DataPoint] = Ordering.by(_.value)
  implicit val asPair: DataPoint => (Time, Double) = dp => (dp.time, dp.value)
}

/**
 * A simple [[Time]] and [[Double value]] pairing.
 *
 * @param time
 *   The time at which the value was recorded
 * @param value
 *   The measured value of that data associated with the given [[time]]
 */
private[timeseries] final case class DataPoint(time: Time, value: Double)
