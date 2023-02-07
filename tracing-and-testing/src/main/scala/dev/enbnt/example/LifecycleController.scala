package dev.enbnt.example

import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class LifecycleController extends Controller {
  
    post("/lifecycle") { _: Request =>
        // this is an example where you have some local process lifecycle
        // information that you want to have available and make visible
        // to trace data.
        val trace = Trace()
        trace.traceLocal("example.lifecycle.init") {
          trace.traceLocal("example.lifecycle.init.sub1") {
          }
          trace.traceLocal("example.lifecycle.init.sub2") {
          }
        }
        trace.traceLocal("example.lifecycle.process") {
        }
        trace.traceLocal("example.lifecycle.end") {
        }

        response.ok()
    }

}
