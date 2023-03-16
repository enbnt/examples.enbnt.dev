package dev.enbnt.timeseries.datasource

import com.twitter.conversions.DurationOps._
import com.twitter.inject.Test
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.immutable.TimeSeries

class InMemoryDataSourceTest extends Test {

  test("InMemoryDataSource#store and retrieve") {
    val interval = 1.second
    val now = Time.now.floor(interval)
    val store = new InMemoryDataSource(interval)
    store.append(
      "requests",
      DataPoint(now, 5),
      tags = Map("method" -> "GET", "success" -> "true", "host" -> "localhost")
    )
    store.append(
      "requests",
      DataPoint(now, 5),
      tags = Map("method" -> "GET", "success" -> "false", "host" -> "localhost")
    )
    store.append(
      "requests",
      DataPoint(now + 1.second, 10),
      tags = Map("method" -> "GET", "success" -> "true", "host" -> "localhost")
    )
    store.append(
      "requests",
      DataPoint(now + 1.second, 10),
      tags = Map("method" -> "GET", "success" -> "false", "host" -> "localhost")
    )
    store.append(
      "requests",
      DataPoint(now + 1.second, 3),
      tags = Map("method" -> "PUT", "success" -> "true", "host" -> "localhost")
    )

    val queryResult = store.apply("requests", now, now + 1.second, interval)
    assert(queryResult.nonEmpty)
    assert(queryResult.size == 1)
    queryResult.head.iterator.sameElements(
      TimeSeries(
        interval = interval,
        data = Array(DataPoint(now, 10), DataPoint(now + 1.second, 23))
      )
    )

    val queryResult2 = store.apply(
      "requests",
      now,
      now + 1.second,
      interval,
      Map("method" -> "GET")
    )
    assert(queryResult2.nonEmpty)
    assert(queryResult2.size == 1)
    queryResult2.head.iterator.sameElements(
      TimeSeries(
        interval = interval,
        data = Array(DataPoint(now, 10), DataPoint(now + 1.second, 20))
      )
    )

    val queryResult3 = store.apply(
      "requests",
      now,
      now + 1.second,
      interval,
      Map("method" -> "GET", "success" -> "false")
    )
    assert(queryResult3.nonEmpty)
    assert(queryResult3.size == 1)
    queryResult3.head.iterator.sameElements(
      TimeSeries(
        interval = interval,
        data = Array(DataPoint(now, 5), DataPoint(now + 1.second, 10))
      )
    )

    val queryResult4 = store.apply(
      "requests",
      now,
      now + 1.second,
      interval,
      Map("method" -> "PUT")
    )
    assert(queryResult4.nonEmpty)
    assert(queryResult4.size == 1)
    queryResult4.head.iterator.sameElements(
      TimeSeries(
        interval = interval,
        data = Array(DataPoint(now + 1.second, 3))
      )
    )

    assert(
      store
        .apply(
          "requests",
          now,
          now + 1.second,
          interval,
          Map("method" -> "POST", "success" -> "true")
        )
        .isEmpty
    )
  }

  test("InMemoryDataSource#ring buffer loops when max size hit") {
    val interval = 1.second
    val now = Time.now.floor(interval)
    val store = new InMemoryDataSource(interval, 4)

    assert(store.apply("requests", Time.Bottom, Time.Top, interval).isEmpty)

    (0 until 4).foreach { i =>
      store.append("requests", DataPoint(now + i.seconds, i))
    }

    val queryResult = store.apply("requests", Time.Bottom, Time.Top, interval)
    assert(queryResult.nonEmpty)
    assert(queryResult.size == 1)
    assert(queryResult.head.size == 4)
    assert(
      queryResult.head == TimeSeries(
        interval,
        Array(
          DataPoint(now, 0),
          DataPoint(now + 1.second, 1),
          DataPoint(now + 2.seconds, 2),
          DataPoint(now + 3.seconds, 3)
        )
      )
    )

    store.append("requests", DataPoint(now + 4.seconds, 4))

    val queryResult2 = store.apply("requests", Time.Bottom, Time.Top, interval)
    assert(queryResult2.nonEmpty)
    assert(queryResult2.size == 1)
    assert(queryResult2.head.size == 4)
    assert(
      queryResult2.head == TimeSeries(
        interval,
        Array(
          DataPoint(now + 1.second, 1),
          DataPoint(now + 2.seconds, 2),
          DataPoint(now + 3.seconds, 3),
          DataPoint(now + 4.seconds, 4)
        )
      )
    )

    store.append("requests", DataPoint(now + 5.seconds, 5))

    val queryResult3 = store.apply("requests", Time.Bottom, Time.Top, interval)
    assert(queryResult2.nonEmpty)
    assert(queryResult2.size == 1)
    assert(queryResult2.head.size == 4)
    assert(
      queryResult3.head == TimeSeries(
        interval,
        Array(
          DataPoint(now + 2.seconds, 2),
          DataPoint(now + 3.seconds, 3),
          DataPoint(now + 4.seconds, 4),
          DataPoint(now + 5.seconds, 5)
        )
      )
    )

  }

  test("InMemoryDataSource#clear()") {
    val interval = 1.second
    val now = Time.now.floor(interval)
    val store = new InMemoryDataSource(interval, 4)

    assert(store.apply("requests", Time.Bottom, Time.Top, interval).isEmpty)

    (0 until 4).foreach { i =>
      store.append("requests", DataPoint(now + i.seconds, i))
    }

    val queryResult = store.apply("requests", Time.Bottom, Time.Top, interval)
    assert(queryResult.nonEmpty)
    assert(queryResult.size == 1)
    assert(queryResult.head.size == 4)
    assert(
      queryResult.head == TimeSeries(
        interval,
        Array(
          DataPoint(now, 0),
          DataPoint(now + 1.second, 1),
          DataPoint(now + 2.seconds, 2),
          DataPoint(now + 3.seconds, 3)
        )
      )
    )

    store.clear()
    assert(store.apply("requests", Time.Bottom, Time.Top, interval).isEmpty)

  }

}
