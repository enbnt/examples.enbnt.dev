package dev.enbnt.example

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

object ExampleHttpServerMain extends ExampleHttpServer

class ExampleHttpServer extends HttpServer {

    // configure our router with our controller
    override protected def configureHttp(router: HttpRouter): Unit = {
      router.add[ExampleController]
    }
}
