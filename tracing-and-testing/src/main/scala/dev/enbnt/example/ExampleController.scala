package dev.enbnt.example

import com.twitter.finagle.tracing.Trace
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import dev.enbnt.example.domain.ScoreRequest
import dev.enbnt.example.domain.ScoreResponse


class ExampleController extends Controller {
    
    get("/score") { request: ScoreRequest =>
        
        val ScoreRequest(optName, optMultiplier) = request

        val score: Int = optName match {
            case Some(name) =>
                                // retrieve this request's active trace context and
                // annotate some application specific data to the trace
                val trace = Trace()
                if (trace.isActivelyTracing) {
                  trace.recordBinary("example.name", name)
                }

                val mult = optMultiplier match {
                    case Some(m) => 
                        trace.recordBinary("example.multiplier", m)
                        m
                    case _ => 1
                }

                name.length * mult

            case _ => 
                -1
        }

        // return the response
        ScoreResponse(optName, score)
    }

}
