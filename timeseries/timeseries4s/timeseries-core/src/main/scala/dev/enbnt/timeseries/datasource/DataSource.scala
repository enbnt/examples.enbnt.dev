package dev.enbnt.timeseries.datasource

import com.twitter.util.Duration
import com.twitter.util.Time
import dev.enbnt.timeseries.common.TimeSeriesLike

// This is ONLY a data layer and it is not concerned with Query DSL/semantics
private[timeseries] trait DataSource {
  def apply(
    metric: String,
    start: Time,
    end: Time,
    interval: Duration,
    tags: Map[String, String] = Map.empty
  ): Iterable[TimeSeriesLike]
}
