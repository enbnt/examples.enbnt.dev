package dev.enbnt.example

import com.google.inject.Stage
import com.twitter.inject.server.FeatureTest
import com.twitter.finatra.http.EmbeddedHttpServer

class ExampleHttpServerStartupTest extends FeatureTest {
  
  override val server = new EmbeddedHttpServer(
    twitterServer = new ExampleHttpServer,
    stage = Stage.PRODUCTION
  )

  test("ExampleServer#startUp") {
    server.start()
    server.assertHealthy()
  }

}
