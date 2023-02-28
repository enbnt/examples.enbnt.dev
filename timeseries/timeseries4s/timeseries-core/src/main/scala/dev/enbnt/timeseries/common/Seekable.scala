package dev.enbnt.timeseries.common

import com.twitter.util.Time

/**
 * A [[Seekable]] represents an indexable [[TimeSeriesLike]], which can be used
 * as a signal for optimized look-ups within time series data (i.e. a binary
 * search)
 */
private[timeseries] trait Seekable extends TimeSeriesLike {

  /**
   * Retrieve the index which corresponds to the associated [[Time time]].
   * @param time
   *   The time to search for.
   * @return
   *   The index associated with the given time, if it is found.
   */
  def indexAt(time: Time): Int

}
