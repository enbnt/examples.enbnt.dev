package dev.enbnt.example

import com.twitter.finatra.http.annotations.QueryParam

final case class ExampleRequest(
    @QueryParam name: Option[String]
)
