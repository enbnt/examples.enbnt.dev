package dev.enbnt.timeseries.datasource

/**
 * A mapping key that associates a metric label and its tag key/value set
 *
 * @note
 *   This is not an optimized structure
 */
private[datasource] final case class TagSet(
  metric: String,
  tags: Set[(String, String)]
)
