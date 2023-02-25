package dev.enbnt.timeseries

import com.twitter.conversions.DurationOps._
import com.twitter.util.{Duration, Time}
import org.scalatest.funsuite.AnyFunSuite
import TimeSeriesOps._
import dev.enbnt.timeseries.common.DataPoint

object TimeSeriesBehaviors {
  case class Input(interval: Duration, values: IndexedSeq[DataPoint])
}

trait TimeSeriesBehaviors { this: AnyFunSuite =>

  def nonEmptyTimeSeries(
    label: String,
    ts: TimeSeriesBehaviors.Input => TimeSeries
  ): Unit = {

    test(s"$label#range outer") {
      Time.withCurrentTimeFrozen { _ =>
        val interval = 1.second
        val start = Time.now.floor(interval) - 5.seconds

        val t0: TimeSeries = ts(
          TimeSeriesBehaviors.Input(
            interval,
            Array(
              DataPoint(start, 1),
              DataPoint(start + 1.second, 2),
              DataPoint(start + 2.seconds, 3),
              DataPoint(start + 3.seconds, 4),
              DataPoint(start + 4.seconds, 5)
            )
          )
        )
        val t1: TimeSeries = t0.range(start - 1.second, start + 8.seconds)

        assert(t1.start == t0.start)
        assert(t1.end == t0.end)
        assert(t1 == t0)
      }

    }

    test(s"$label#range middle") {
      Time.withCurrentTimeFrozen { _ =>
        val interval = 1.second
        val start = Time.now.floor(interval) - 5.seconds

        val t0: TimeSeries = ts(
          TimeSeriesBehaviors.Input(
            interval,
            Array(
              DataPoint(start, 1),
              DataPoint(start + 1.second, 2),
              DataPoint(start + 2.seconds, 3),
              DataPoint(start + 3.seconds, 4),
              DataPoint(start + 4.seconds, 5)
            )
          )
        )
        val t1: TimeSeries = t0.range(start + 1.second, start + 3.seconds)

        assert(t1 == TimeSeries(interval, t0.drop(1).take(3)))
        assert(t1.start == start + 1.second, t1)
        assert(t1.end == start + 3.seconds, t1)

      }

    }

    test(s"$label#range begin") {
      Time.withCurrentTimeFrozen { _ =>
        val interval = 1.second
        val start = Time.now.floor(interval) - 5.seconds

        val t0: TimeSeries = ts(
          TimeSeriesBehaviors.Input(
            interval,
            Array(
              DataPoint(start, 1),
              DataPoint(start + 1.second, 2),
              DataPoint(start + 2.seconds, 3),
              DataPoint(start + 3.seconds, 4),
              DataPoint(start + 4.seconds, 5)
            )
          )
        )
        val t1: TimeSeries = t0.range(start, start + 3.seconds)

        assert(t1 == TimeSeries(interval, t0.take(4)))
        assert(t1.start == start)
        assert(t1.end == start + 3.seconds)

      }
    }

    test(s"$label#range not within time series") {
      Time.withCurrentTimeFrozen { _ =>
        val interval = 1.second
        val start = Time.now.floor(interval) - 5.seconds

        val t0: TimeSeries = ts(
          TimeSeriesBehaviors.Input(
            interval,
            Array(
              DataPoint(start, 1),
              DataPoint(start + 1.second, 2),
              DataPoint(start + 2.seconds, 3),
              DataPoint(start + 3.seconds, 4),
              DataPoint(start + 4.seconds, 5)
            )
          )
        )
        val t1: TimeSeries = t0.range(start - 5.seconds, start - 3.seconds)

        assert(t1 == TimeSeries.empty())

        val t2: TimeSeries = t0.range(start + 5.seconds, start + 8.seconds)

        assert(t2 == TimeSeries.empty())
      }

    }

    test(s"$label#range returns single value") {
      Time.withCurrentTimeFrozen { _ =>
        val interval = 1.second
        val start = Time.now.floor(interval) - 5.seconds

        val t0: TimeSeries = ts(
          TimeSeriesBehaviors.Input(
            interval,
            Array(
              DataPoint(start, 1),
              DataPoint(start + 1.second, 2),
              DataPoint(start + 2.seconds, 3),
              DataPoint(start + 3.seconds, 4),
              DataPoint(start + 4.seconds, 5)
            )
          )
        )

        val t1: TimeSeries = t0.range(start + 3.seconds, start + 3.seconds)

        assert(
          t1 == TimeSeries(interval, Array(DataPoint(start + 3.seconds, 4)))
        )
      }

    }

  }

  def emptyTimeSeries(ts: => TimeSeries): Unit = {
    test("EmptyTimeSeries#empty iterator") {
      assert(ts.isEmpty)
      assert(ts.iterator.isEmpty)
    }
    test("EmptyTimeSeries#range") {
      assert(ts.range(Time.Bottom, Time.Top).isEmpty)
    }
  }

}
