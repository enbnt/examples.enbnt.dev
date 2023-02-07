package dev.enbnt.example

import com.twitter.finagle.tracing.Trace
import com.twitter.finatra.http.Controller

class ExampleController extends Controller {
    
    get("/") { request: ExampleRequest =>
        
        val score = request match {
            case ExampleRequest(Some(name)) =>
                // retrieve this request's active trace context and
                // annotate some application specific data to the trace
                val trace = Trace()
                trace.recordBinary("name", request.name)

                // calculate a score for the name
                name.length * 2
            case _ =>
                -1
        }

        // return the response
        ExampleResponse(request.name, score)
    }
}
