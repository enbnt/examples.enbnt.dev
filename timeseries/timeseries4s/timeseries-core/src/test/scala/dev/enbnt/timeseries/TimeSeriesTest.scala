package dev.enbnt.timeseries

import com.twitter.inject.Test

class TimeSeriesTest extends Test with TimeSeriesBehaviors {

  testsFor(
    nonEmptyTimeSeries(
      "DenseTimeSeries",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        new immutable.DenseTimeSeries(
          interval,
          values.head.time,
          values.map(_.value)
        )
      }
    )
  )

  testsFor(
    nonEmptyTimeSeries(
      "SparseTimeSeries",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        val (t, v) = values.unzip
        new immutable.SparseTimeSeries(interval, t, v)
      }
    )
  )

  testsFor(
    nonEmptyTimeSeries(
      "CircularBufferTimeSeries",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        val ts = new mutable.CircularBufferTimeSeries(interval, values.size)
        ts.write(values.toSeq: _*)
        ts
      }
    )
  )

  testsFor(
    nonEmptyTimeSeries(
      "TimeSeries.apply",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        TimeSeries(interval, values)
      }
    )
  )

  testsFor(emptyTimeSeries(TimeSeries.empty()))

}
