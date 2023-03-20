package dev.enbnt.timeseries.common

/** [[Value]] is a marker trait for Time Series value data. */
sealed trait Value extends Any {

  /** A comparator between [[Value]] types priority */
  def castPriority: Int

  /** Convert [[Value v]] to [[this]] [[Value]] type */
  def recast(v: Value): Value

  def asInt: Int
  def asLong: Long
  def asFloat: Float
  def asDouble: Double

  def +(other: Value): Value
  def -(other: Value): Value
  def *(other: Value): Value
  def /(other: Value): Value

}

object Value {
  implicit val ordering: Ordering[Value] = Ordering.by {
    case FloatVal(v)  => v.toDouble
    case DoubleVal(v) => v
    case LongVal(v)   => v.toDouble
    case IntVal(v)    => v.toDouble
    case Undefined    => Double.NaN
  }

  // follow behavior of operating across int, long, double, and float
  // types, while still supporting Undefined across all operations.
  // this will take the type with priority amongst given values and
  // re-cast the lower type to match (int -> long -> float -> double -> undefined)
  private[this] def upcast(v1: Value, v2: Value)(
    f: (Value, Value) => Value
  ): Value = {
    val diff = v1.castPriority - v2.castPriority
    if (diff == 0) {
      f(v1, v2)
    } else if (diff > 0) {
      f(v1, v1.recast(v2))
    } else {
      f(v2.recast(v1), v2)
    }
  }

  case class FloatVal(value: Float) extends AnyVal with Value {
    override def asInt: Int = value.toInt
    override def asLong: Long = value.toLong
    override def asFloat: Float = value
    override def asDouble: Double = value.toDouble

    override def +(other: Value): Value = other match {
      case FloatVal(v) => FloatVal(value + v.value)
      case _           => upcast(this, other)(_ + _)
    }

    override def -(other: Value): Value = other match {
      case FloatVal(v) => FloatVal(value - v.value)
      case _           => upcast(this, other)(_ - _)
    }
    override def *(other: Value): Value = other match {
      case FloatVal(v) => FloatVal(value * v.value)
      case _           => upcast(this, other)(_ * _)
    }

    override def /(other: Value): Value = other match {
      case FloatVal(v) => FloatVal(value / v.value)
      case _           => upcast(this, other)(_ / _)
    }

    override def castPriority: Int = 2

    override def recast(v: Value): Value = FloatVal(v.asFloat)
  }
  case class DoubleVal(value: Double) extends AnyVal with Value {
    override def asInt: Int = value.toInt
    override def asLong: Long = value.toLong

    override def asFloat: Float = value.toFloat

    override def asDouble: Double = value

    override def +(other: Value): Value = other match {
      case DoubleVal(v) => DoubleVal(value + v.value)
      case _            => upcast(this, other)(_ + _)
    }

    override def -(other: Value): Value = other match {
      case DoubleVal(v) => DoubleVal(value - v.value)
      case _            => upcast(this, other)(_ - _)
    }

    override def *(other: Value): Value = other match {
      case DoubleVal(v) => DoubleVal(value * v.value)
      case _            => upcast(this, other)(_ * _)
    }

    override def /(other: Value): Value = other match {
      case DoubleVal(v) => DoubleVal(value / v.value)
      case _            => upcast(this, other)(_ / _)
    }

    override def castPriority: Int = 3

    override def recast(v: Value): Value = DoubleVal(v.asDouble)
  }
  case class IntVal(value: Int) extends AnyVal with Value {
    override def asInt: Int = value
    override def asLong: Long = value.toLong

    override def asFloat: Float = value.toFloat

    override def asDouble: Double = value.toDouble

    override def +(other: Value): Value = other match {
      case IntVal(v) => IntVal(value + v.value)
      case _         => upcast(this, other)(_ + _)
    }

    override def -(other: Value): Value = other match {
      case IntVal(v) => IntVal(value - v.value)
      case _         => upcast(this, other)(_ - _)
    }

    override def *(other: Value): Value = other match {
      case IntVal(v) => IntVal(value * v.value)
      case _         => upcast(this, other)(_ * _)
    }

    override def /(other: Value): Value = other match {
      case IntVal(v) => IntVal(value / v.value)
      case _         => upcast(this, other)(_ / _)
    }

    override def castPriority: Int = 0

    override def recast(v: Value): Value = IntVal(v.asInt)
  }
  case class LongVal(value: Long) extends AnyVal with Value {
    override def asInt: Int = value.toInt
    override def asLong: Long = value

    override def asFloat: Float = value.toFloat

    override def asDouble: Double = value.toDouble

    override def +(other: Value): Value = other match {
      case LongVal(v) => LongVal(value + v.value)
      case _          => upcast(this, other)(_ + _)
    }

    override def -(other: Value): Value = other match {
      case LongVal(v) => LongVal(value - v.value)
      case _          => upcast(this, other)(_ - _)
    }

    override def *(other: Value): Value = other match {
      case LongVal(v) => LongVal(value * v.value)
      case _          => upcast(this, other)(_ * _)
    }

    override def /(other: Value): Value = other match {
      case LongVal(v) => LongVal(value / v.value)
      case _          => upcast(this, other)(_ / _)
    }

    override def castPriority: Int = 1

    override def recast(v: Value): Value = LongVal(v.asLong)
  }
  case object Undefined extends Value {
    override def asInt: Int = throw new IllegalStateException()

    override def asLong: Long = throw new IllegalStateException()

    override def asFloat: Float = Float.NaN

    override def asDouble: Double = Double.NaN

    override def +(other: Value): Value = this

    override def -(other: Value): Value = this

    override def *(other: Value): Value = this

    override def /(other: Value): Value = this

    override def castPriority: Int = Int.MaxValue

    override def recast(v: Value): Value = this
  }

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
