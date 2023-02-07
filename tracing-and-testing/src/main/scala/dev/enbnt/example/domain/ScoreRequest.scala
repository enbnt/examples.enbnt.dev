package dev.enbnt.example.domain

import com.twitter.finatra.http.annotations.QueryParam

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max

final case class ScoreRequest(
    @QueryParam name: Option[String],
    @QueryParam @Min(1) @Max(10) multiplier: Option[Int]
)
