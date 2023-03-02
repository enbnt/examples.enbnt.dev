package dev.enbnt.timeseries

import com.twitter.inject.Test

class TimeSeriesTest extends Test with TimeSeriesBehaviors {

  testsFor(
    nonEmptyTimeSeries(
      "DenseTimeSeries",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        immutable.DenseTimeSeries(interval, values)
      }
    )
  )

  testsFor(
    nonEmptyTimeSeries(
      "SparseTimeSeries",
      { case TimeSeriesBehaviors.Input(interval, values) =>
        immutable.SparseTimeSeries(interval, values)
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
