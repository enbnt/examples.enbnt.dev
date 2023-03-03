package dev.enbnt.timeseries.common

import dev.enbnt.timeseries.common.Value._

object DataPointOps {

  implicit class RichDataPoint(val dp: DataPoint) extends AnyVal {

    def +(other: DataPoint): DataPoint = applyOpDp(other)(_ + _)
    def +(v: Value): DataPoint = applyOpValue(v)(_ + _)
    def +(f: Float): DataPoint = dp + f.asValue
    def +(d: Double): DataPoint = dp + d.asValue
    def +(i: Int): DataPoint = dp + i.asValue
    def +(l: Long): DataPoint = dp + l.asValue

    def -(other: DataPoint): DataPoint = applyOpDp(other)(_ - _)
    def -(v: Value): DataPoint = applyOpValue(v)(_ - _)
    def -(v: Float): DataPoint = dp - v.asValue
    def -(v: Double): DataPoint = dp - v.asValue
    def -(v: Int): DataPoint = dp - v.asValue
    def -(v: Long): DataPoint = dp - v.asValue

    def /(other: DataPoint): DataPoint = applyOpDp(other)(_ / _)
    def /(v: Value): DataPoint = applyOpValue(v)(_ / _)
    def /(v: Float): DataPoint = dp / v.asValue
    def /(v: Double): DataPoint = dp / v.asValue
    def /(v: Int): DataPoint = dp / v.asValue
    def /(v: Long): DataPoint = dp / v.asValue

    def *(other: DataPoint): DataPoint = applyOpDp(other)(_ * _)
    def *(v: Value): DataPoint = applyOpValue(v)(_ * _)
    def *(v: Float): DataPoint = dp * v.asValue
    def *(v: Double): DataPoint = dp * v.asValue
    def *(v: Int): DataPoint = dp * v.asValue
    def *(v: Long): DataPoint = dp * v.asValue

    private[this] def applyOpDp(
      dp2: DataPoint
    )(f: (Value, Value) => Value): DataPoint = {
      require(dp.time == dp2.time)
      applyOpValue(dp2.value)(f)
    }
    private[this] def applyOpValue(v: Value)(
      f: (Value, Value) => Value
    ): DataPoint = DataPoint(dp.time, f(dp.value, v))

  }

}
