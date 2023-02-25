package dev.enbnt.timeseries.common

import com.twitter.util.Time

object DataPoint {
  implicit val timeOrdering: Ordering[DataPoint] = Ordering.by(_.time)
  implicit val valueOrdering: Ordering[DataPoint] = Ordering.by(_.value)
  implicit val asPair: DataPoint => (Time, Double) = dp => (dp.time, dp.value)
}

final case class DataPoint(time: Time, value: Double)
