package dev.enbnt.example

import com.twitter.finagle.http.Status._
import com.twitter.inject.server.FeatureTest
import com.twitter.finatra.http.EmbeddedHttpServer

class ExampleHttpServerFeatureTest extends FeatureTest {
  
  // note: we use 'val' instead of 'def' to prevent a new server from being created
  // for each test case, which can be expensive
  override val server = new EmbeddedHttpServer(
    twitterServer = new ExampleHttpServer
  )

  test("ExampleServer#scores correctly with 'name' query param specified") {
    server.httpGet(
      "/score?name=enbnt", 
      andExpect = Ok, 
      withJsonBody = """{"name": "enbnt", "score": 5}"""
    )

    // verify our trace annotation is present
    server.inMemoryTracer.binaryAnnotations("example.name", "enbnt")
    server.inMemoryTracer.binaryAnnotations.get("example.multiplier") shouldBe None
  }

  test("ExampleServer#scores correctly with 'name' and 'multiplier' query param specified") {
    server.httpGet(
      "/score?name=ian&multiplier=5", 
      andExpect = Ok, 
      withJsonBody = """{"name": "ian", "score": 15}"""
    )

    // verify our trace annotation is present
    server.inMemoryTracer.binaryAnnotations("example.name", "ian")
    server.inMemoryTracer.binaryAnnotations("example.multiplier", 5)
  }

  test("ExampleServer#scores correctly without 'name' query param specified") {
    server.httpGet(
      "/score", 
      andExpect = Ok, 
      withJsonBody = """{"score": -1}"""
    )
    
    // verify that no trace annotation is present
    server.inMemoryTracer.binaryAnnotations.get("example.name") shouldBe None
    server.inMemoryTracer.binaryAnnotations.get("example.multiplier") shouldBe None
  }

  test("ExampleServer#scores correctly without 'name' query param specified, but with 'multiplier' query param specified") {
    server.httpGet(
      "/score?multiplier=2", 
      andExpect = Ok, 
      withJsonBody = """{"score": -1}"""
    )
    
    // verify that no trace annotation is present
    server.inMemoryTracer.binaryAnnotations.get("example.name") shouldBe None
    server.inMemoryTracer.binaryAnnotations.get("example.multiplier") shouldBe None
  }

  test("ExampleServer#processes a lifecycle request") {
    server.httpPost(
      "/lifecycle", 
      postBody = "",
      andExpect = Ok,
      withBody = "complete"
    )
    
    server.inMemoryTracer.rpcs("example.lifecycle.init")
    server.inMemoryTracer.rpcs("example.lifecycle.init.sub1")
    server.inMemoryTracer.rpcs("example.lifecycle.init.sub2")
    server.inMemoryTracer.rpcs("example.lifecycle.process")
    server.inMemoryTracer.rpcs("example.lifecycle.end")
  }

}
