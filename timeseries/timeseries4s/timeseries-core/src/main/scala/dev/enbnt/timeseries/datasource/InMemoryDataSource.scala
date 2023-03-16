package dev.enbnt.timeseries.datasource

import com.twitter.util.Duration
import com.twitter.util.Time
import dev.enbnt.timeseries.common.DataPoint
import dev.enbnt.timeseries.common.TimeSeriesLike
import dev.enbnt.timeseries.mutable
import dev.enbnt.timeseries.mutable.CircularBufferTimeSeries
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.MapHasAsScala

/**
 * A bounded in-memory [[DataSource]] implementation.
 *
 * @param interval
 *   The [[Duration interval]] of each underlying [[TimeSeriesLike time series]]
 * @param tsMaxSize
 *   The maximum size of each underlying [[TimeSeriesLike time series]]
 *
 * @note
 *   This class is intended to be used as a basic, local testing utility and is
 *   not intended for large-scale production use.
 */
private[timeseries] final class InMemoryDataSource(
  interval: Duration,
  tsMaxSize: Int = 512
) extends DataSource {

  require(
    tsMaxSize > 0,
    "InMemoryDataSource must have a maximum time series size > 0"
  )

  private[this] val data
    : scala.collection.mutable.Map[TagSet, mutable.TimeSeries] =
    new ConcurrentHashMap[TagSet, mutable.TimeSeries].asScala

  def append(
    label: String,
    dataPoint: DataPoint,
    tags: Map[String, String] = Map.empty
  ): Unit = {
    // each tag permutation results in a new time series to track
    tags.toSet.subsets().foreach { tags =>
      val ts = data.getOrElseUpdate(
        TagSet(label, tags),
        new CircularBufferTimeSeries(interval, tsMaxSize)
      )

      // TODO - CircularBufferTimeSeries is not thread-safe, but our TagSet mapping is
      ts synchronized {
        ts.append(dataPoint)
      }
    }
  }

  def clear(): Unit = data.clear()

  override def apply(
    metric: String,
    start: Time,
    end: Time,
    interval: Duration,
    tags: Map[String, String] = Map.empty
  ): Iterable[TimeSeriesLike] = {
    if (interval != this.interval) {
      Iterable.empty
    } else {
      val tagSet: Set[(String, String)] =
        if (tags.isEmpty) Set.empty else tags.toSet

      data.get(TagSet(metric, tagSet)) match {
        case Some(ts) => Iterable.single(ts)
        case _        => Iterable.empty
      }
    }

  }
}
