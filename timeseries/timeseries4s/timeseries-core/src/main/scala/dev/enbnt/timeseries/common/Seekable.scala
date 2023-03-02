package dev.enbnt.timeseries.common

import com.twitter.util.Time
import scala.collection.Searching.SearchResult

/**
 * A [[Seekable]] represents an indexable [[TimeSeriesLike]], which can be used
 * as a signal for optimized look-ups within time series data (i.e. a binary
 * search)
 */
private[timeseries] trait Seekable extends TimeSeriesLike {

  /**
   * Search for an index that corresponds to the given [[Time time]].
   * @param time
   *   The time to search for.
   * @return
   *   The [[SearchResult]] associated with the given time.
   */
  def indexAt(time: Time): SearchResult

  /** Return the [[DataPoint]] defined at the given index */
  def apply(idx: Int): DataPoint

}
