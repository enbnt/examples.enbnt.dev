package dev.enbnt.timeseries.common

import com.twitter.inject.Test
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPointOps._
import dev.enbnt.timeseries.common.Value._

class DataPointOpsTest extends Test {

  private[this] val t = Time.now

  test("DataPointOps#add two data points") {
    // int + int
    assert(DataPoint(t, 5) + DataPoint(t, 10) == DataPoint(t, 15))
    // int + undefined
    assert(
      DataPoint(t, 5) + DataPoint(t, Value.Undefined) == DataPoint(
        t,
        Value.Undefined
      )
    )
    // int + double
    assert(DataPoint(t, 5) + DataPoint(t, 1.5d) == DataPoint(t, 6.5d))
    // double + int
    assert(DataPoint(t, 1.5d) + DataPoint(t, 5) == DataPoint(t, 6.5d))
    // int + float
    assert(DataPoint(t, 5) + DataPoint(t, 2.5f) == DataPoint(t, 7.5f))
    // float + int
    assert(DataPoint(t, 2.5f) + DataPoint(t, 5) == DataPoint(t, 7.5f))
    // int + long
    assert(DataPoint(t, 5) + DataPoint(t, 200L) == DataPoint(t, 205L))
    // long + long
    assert(DataPoint(t, 200L) + DataPoint(t, 500L) == DataPoint(t, 700L))
    // long + int
    assert(DataPoint(t, 200L) + DataPoint(t, 5) == DataPoint(t, 205L))
    // long + float
    assert(DataPoint(t, 200L) + DataPoint(t, 52f) == DataPoint(t, 252f))
    // float + long
    assert(DataPoint(t, 52f) + DataPoint(t, 200L) == DataPoint(t, 252f))
    // long + double
    assert(DataPoint(t, 200L) + DataPoint(t, 55d) == DataPoint(t, 255d))
    // double + long
    assert(DataPoint(t, 55d) + DataPoint(t, 200L) == DataPoint(t, 255d))
    // float + float
    assert(DataPoint(t, 12.5f) + DataPoint(t, 7.25f) == DataPoint(t, 19.75f))
    // double + double
    assert(
      DataPoint(t, 55.12d) + DataPoint(t, 1.22d) == DataPoint(t, 55.12d + 1.22d)
    )
    // float + double
    assert(DataPoint(t, 55.25f) + DataPoint(t, 25.25d) == DataPoint(t, 80.5d))
    // double + float
    assert(DataPoint(t, 25.25d) + DataPoint(t, 55.25f) == DataPoint(t, 80.5d))
  }

  test("DataPointOps#subtract two data points") {
    // int - int
    assert(DataPoint(t, 5) - DataPoint(t, 2) == DataPoint(t, 3))
    // int - undefined
    assert(
      DataPoint(t, 5) - DataPoint(t, Value.Undefined) == DataPoint(
        t,
        Value.Undefined
      )
    )
    // int - double
    assert(DataPoint(t, 5) - DataPoint(t, 1.5d) == DataPoint(t, 3.5d))
    // double - int
    assert(DataPoint(t, 6.5d) - DataPoint(t, 5) == DataPoint(t, 1.5d))
    // int - float
    assert(DataPoint(t, 5) - DataPoint(t, 2.5f) == DataPoint(t, 2.5f))
    // float - int
    assert(DataPoint(t, 7.5f) - DataPoint(t, 5) == DataPoint(t, 2.5f))
    // int - long
    assert(DataPoint(t, 5) - DataPoint(t, 200L) == DataPoint(t, -195L))
    // long - long
    assert(DataPoint(t, 200L) - DataPoint(t, 500L) == DataPoint(t, -300L))
    // long - int
    assert(DataPoint(t, 200L) - DataPoint(t, 5) == DataPoint(t, 195L))
    // long - float
    assert(DataPoint(t, 200L) - DataPoint(t, 52.5f) == DataPoint(t, 147.5f))
    // float - long
    assert(DataPoint(t, 52.6f) - DataPoint(t, 20L) == DataPoint(t, 32.6f))
    // long - double
    assert(
      DataPoint(t, 200L) - DataPoint(t, 55.33d) == DataPoint(t, 200L - 55.33d)
    )
    // double - long
    assert(DataPoint(t, 55d) - DataPoint(t, 200L) == DataPoint(t, -145d))
    // float - float
    assert(DataPoint(t, 12.5f) - DataPoint(t, 7.25f) == DataPoint(t, 5.25f))
    // double - double
    assert(
      DataPoint(t, 55.12d) - DataPoint(t, 1.22d) == DataPoint(t, 55.12d - 1.22d)
    )
    // float - double
    assert(DataPoint(t, 55.25f) - DataPoint(t, 25.25d) == DataPoint(t, 30d))
    // double - float
    assert(DataPoint(t, 25.25d) - DataPoint(t, 55.25f) == DataPoint(t, -30d))
  }

  test("DataPointOps#multiply two data points") {
    // int * int
    assert(DataPoint(t, 5) * DataPoint(t, 2) == DataPoint(t, 10))
    // int * undefined
    assert(
      DataPoint(t, 5) * DataPoint(t, Value.Undefined) == DataPoint(
        t,
        Value.Undefined
      )
    )
    // int * double
    assert(DataPoint(t, 5) * DataPoint(t, 1.5d) == DataPoint(t, 7.5))
    // double * int
    assert(DataPoint(t, -6.5d) * DataPoint(t, 5) == DataPoint(t, -32.5d))
    // int * float
    assert(DataPoint(t, 5) * DataPoint(t, 2.5f) == DataPoint(t, 12.5f))
    // float * int
    assert(DataPoint(t, 7.5f) * DataPoint(t, 5) == DataPoint(t, 37.5f))
    // int * long
    assert(DataPoint(t, 5) * DataPoint(t, 200L) == DataPoint(t, 1000L))
    // long * long
    assert(DataPoint(t, 200L) * DataPoint(t, 500L) == DataPoint(t, 100000L))
    // long * int
    assert(DataPoint(t, 200L) * DataPoint(t, 5) == DataPoint(t, 1000L))
    // long * float
    assert(DataPoint(t, 200L) * DataPoint(t, 52.5f) == DataPoint(t, 10500f))
    // float * long
    assert(DataPoint(t, 52.6f) * DataPoint(t, 20L) == DataPoint(t, 1052f))
    // long * double
    assert(
      DataPoint(t, 200L) * DataPoint(t, 55.33d) == DataPoint(t, 200L * 55.33d)
    )
    // double * long
    assert(DataPoint(t, 55d) * DataPoint(t, 200L) == DataPoint(t, 55d * 200L))
    // float * float
    assert(DataPoint(t, 12.5f) * DataPoint(t, 7.25f) == DataPoint(t, 90.625f))
    // double * double
    assert(
      DataPoint(t, 55.12d) * DataPoint(t, 1.22d) == DataPoint(t, 55.12d * 1.22d)
    )
    // float * double
    assert(
      DataPoint(t, 55.25f) * DataPoint(t, 25.25d) == DataPoint(t, 1395.0625d)
    )
    // double * float
    assert(
      DataPoint(t, 25.25d) * DataPoint(t, 55.25f) == DataPoint(t, 1395.0625d)
    )
  }

  test("DataPointOps#divide two data points") {
    // int / int
    assert(DataPoint(t, 10) / DataPoint(t, 2) == DataPoint(t, 5))
    assert(DataPoint(t, 5) / DataPoint(t, 2) == DataPoint(t, 2))
    // int / undefined
    assert(
      DataPoint(t, 5) / DataPoint(t, Value.Undefined) == DataPoint(
        t,
        Value.Undefined
      )
    )
    // int / double
    assert(DataPoint(t, 5) / DataPoint(t, 1.5d) == DataPoint(t, 5 / 1.5d))
    // double / int
    assert(DataPoint(t, -6.5d) / DataPoint(t, 5) == DataPoint(t, -6.5d / 5))
    // int / float
    assert(DataPoint(t, 5) / DataPoint(t, 2.5f) == DataPoint(t, 2f))
    // float / int
    assert(DataPoint(t, 7.5f) / DataPoint(t, 5) == DataPoint(t, 7.5f / 5))
    // int / long
    assert(DataPoint(t, 5) / DataPoint(t, 200L) == DataPoint(t, 0L))
    // long / long
    assert(DataPoint(t, 200L) / DataPoint(t, 500L) == DataPoint(t, 0L))
    // long / int
    assert(DataPoint(t, 200L) / DataPoint(t, 5) == DataPoint(t, 40L))
    // long / float
    assert(
      DataPoint(t, 200L) / DataPoint(t, 52.5f) == DataPoint(t, 200L / 52.5f)
    )
    // float / long
    assert(DataPoint(t, 52.6f) / DataPoint(t, 20L) == DataPoint(t, 52.6f / 20L))
    // long / double
    assert(
      DataPoint(t, 200L) / DataPoint(t, 55.33d) == DataPoint(t, 200L / 55.33d)
    )
    // double / long
    assert(DataPoint(t, 55d) / DataPoint(t, 200L) == DataPoint(t, 55d / 200L))
    // float / float
    assert(
      DataPoint(t, 12.5f) / DataPoint(t, 7.25f) == DataPoint(t, 12.5f / 7.25f)
    )
    // double / double
    assert(
      DataPoint(t, 55.12d) / DataPoint(t, 1.22d) == DataPoint(t, 55.12d / 1.22d)
    )
    // float / double
    assert(
      DataPoint(t, 55.25f) / DataPoint(t, 25.25d) == DataPoint(
        t,
        55.25f / 25.25d
      )
    )
    // double / float
    assert(
      DataPoint(t, 25.25d) / DataPoint(t, 55.25f) == DataPoint(
        t,
        25.25d / 55.25f
      )
    )
  }

  test("DataPointOps#add value to data point") {
    assert(DataPoint(t, 5) + 5.asValue == DataPoint(t, 10))
    assert(DataPoint(t, 5) + 5 == DataPoint(t, 10))
    assert(DataPoint(t, 5) + 5L.asValue == DataPoint(t, 10L))
    assert(DataPoint(t, 5) + 5L == DataPoint(t, 10L))
    assert(DataPoint(t, 5) + 5f.asValue == DataPoint(t, 10f))
    assert(DataPoint(t, 5) + 5f == DataPoint(t, 10f))
    assert(DataPoint(t, 5) + 5d.asValue == DataPoint(t, 10d))
    assert(DataPoint(t, 5) + 5d == DataPoint(t, 10d))
    assert(DataPoint(t, 5) + Value.Undefined == DataPoint(t, Value.Undefined))
  }

  test("DataPointOps#subtract value from data point") {
    assert(DataPoint(t, 5) - 5.asValue == DataPoint(t, 0))
    assert(DataPoint(t, 5) - 5 == DataPoint(t, 0))
    assert(DataPoint(t, 5) - 5L.asValue == DataPoint(t, 0L))
    assert(DataPoint(t, 5) - 5L == DataPoint(t, 0L))
    assert(DataPoint(t, 5) - 5f.asValue == DataPoint(t, 0f))
    assert(DataPoint(t, 5) - 5f == DataPoint(t, 0f))
    assert(DataPoint(t, 5) - 5d.asValue == DataPoint(t, 0d))
    assert(DataPoint(t, 5) - 5d == DataPoint(t, 0d))
    assert(DataPoint(t, 5) - Value.Undefined == DataPoint(t, Value.Undefined))
  }

  test("DataPointOps#multiply value to data point") {
    assert(DataPoint(t, 5) * 5.asValue == DataPoint(t, 25))
    assert(DataPoint(t, 5) * 5 == DataPoint(t, 25))
    assert(DataPoint(t, 5) * 5L.asValue == DataPoint(t, 25L))
    assert(DataPoint(t, 5) * 5L == DataPoint(t, 25L))
    assert(DataPoint(t, 5) * 5f.asValue == DataPoint(t, 25f))
    assert(DataPoint(t, 5) * 5f == DataPoint(t, 25f))
    assert(DataPoint(t, 5) * 5d.asValue == DataPoint(t, 25d))
    assert(DataPoint(t, 5) * 5d == DataPoint(t, 25d))
    assert(DataPoint(t, 5) * Value.Undefined == DataPoint(t, Value.Undefined))
  }

  test("DataPointOps#divide value to data point") {
    assert(DataPoint(t, 5) / 5.asValue == DataPoint(t, 1))
    assert(DataPoint(t, 5) / 5 == DataPoint(t, 1))
    assert(DataPoint(t, 5) / 5L.asValue == DataPoint(t, 1L))
    assert(DataPoint(t, 5) / 5L == DataPoint(t, 1L))
    assert(DataPoint(t, 5) / 5f.asValue == DataPoint(t, 1f))
    assert(DataPoint(t, 5) / 5f == DataPoint(t, 1f))
    assert(DataPoint(t, 5) / 5d.asValue == DataPoint(t, 1d))
    assert(DataPoint(t, 5) / 5d == DataPoint(t, 1d))
    assert(DataPoint(t, 5) / Value.Undefined == DataPoint(t, Value.Undefined))
  }

}
