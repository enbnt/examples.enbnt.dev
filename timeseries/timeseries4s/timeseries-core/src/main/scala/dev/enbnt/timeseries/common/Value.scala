package dev.enbnt.timeseries.common
import dev.enbnt.timeseries.common.Value.Undefined

/** [[Value]] is a marker trait for Time Series value data. */
sealed trait Value extends Any

object Value {
  implicit val ordering: Ordering[Value] = Ordering.by {
    case FloatVal(v)  => v
    case DoubleVal(v) => v
    case LongVal(v)   => v
    case IntVal(v)    => v
    case Undefined    => Double.NaN
  }

  case class FloatVal(value: Float) extends AnyVal with Value
  case class DoubleVal(value: Double) extends AnyVal with Value
  case class IntVal(value: Int) extends AnyVal with Value
  case class LongVal(value: Long) extends AnyVal with Value
  case object Undefined extends Value

  implicit class RichFloat(val value: Float) extends AnyVal {
    def asValue: Value = if (value.isNaN) Undefined else FloatVal(value)
  }

  implicit class RichDouble(val value: Double) extends AnyVal {
    def asValue: Value = if (value.isNaN) Undefined else DoubleVal(value)
  }

  implicit class RichInt(val value: Int) extends AnyVal {
    def asValue: Value = IntVal(value)
  }

  implicit class RichLong(val value: Long) extends AnyVal {
    def asValue: Value = LongVal(value)
  }

//  /**
//   * @param t
//   * @tparam T
//   *
//   * @note
//   *   Struct types are always assumed to be unordered
//   */
//  case class Struct[T](value: T) extends AnyVal with Value
}
