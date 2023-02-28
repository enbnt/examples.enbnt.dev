package dev.enbnt.example

import com.google.inject.Stage
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

class ExampleHttpServerStartupTest extends FeatureTest {

  /**
   * A StartupTest is meant to have no (or minimal) mocks or bindings to the
   * object graph in order to validate that the server can start in as close as
   * a production sense as possible.
   */
  override val server = new EmbeddedHttpServer(
    twitterServer = new ExampleHttpServer,
    stage = Stage.PRODUCTION
  )

  test("ExampleServer#startUp") {
    server.assertHealthy()
  }

}
