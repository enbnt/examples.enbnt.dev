package dev.enbnt.timeseries

import com.twitter.inject.Test

class TimeseriesTest extends Test with TimeseriesBehaviors {

  testsFor(
    nonEmptyTimeseries(
      "DenseTimeseries",
      { case TimeseriesBehaviors.Input(interval, values) =>
        new DenseTimeseries(interval, values.head.time, values.map(_.value))
      }
    )
  )

  testsFor(
    nonEmptyTimeseries(
      "SparseTimeseries",
      { case TimeseriesBehaviors.Input(interval, values) =>
        val (t, v) = values.unzip
        new SparseTimeseries(
          interval,
          t,
          v
        )
      }
    )
  )

  testsFor(
    nonEmptyTimeseries(
      "Timeseries.apply",
      { case TimeseriesBehaviors.Input(interval, values) =>
        Timeseries(
          interval,
          values
        )
      }
    )
  )

  testsFor(emptyTimeseries(EmptyTimeseries))

}
