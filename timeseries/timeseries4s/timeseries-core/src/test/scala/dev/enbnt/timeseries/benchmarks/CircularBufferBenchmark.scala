package dev.enbnt.timeseries.benchmarks

import dev.enbnt.timeseries.util.CircularBuffer
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole

//`bazel run //timeseries/timeseries4s/timeseries-core/src/test/scala/dev/enbnt/timeseries/benchmarks:jmh -- 'CircularBufferBenchmark'`

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
class CircularBufferBenchmark {

  private var cb: CircularBuffer[String] = _

  @Param(Array("256", "1024", "65636"))
  var maxSize: Int = _
  private var loop: Range = _

  @Setup(Level.Invocation)
  def setup(): Unit = {
    cb = new CircularBuffer[String](maxSize)
    loop = (0 until maxSize)
    loop.foreach { _ =>
      cb.write("hello")
    }
  }

  @Benchmark
  def fillBuffer(blackhole: Blackhole): Unit = {
    loop.foreach { _ =>
      cb.write("hello")
    }
    blackhole.consume(cb)
  }

  @Benchmark
  def drainBuffer(blackhole: Blackhole): Unit = {
    loop.foreach { _ =>
      blackhole.consume(cb.read())
    }
  }

  @Benchmark
  def writeThenRead(blackhole: Blackhole): Unit = {
    loop.foreach { _ =>
      cb.write("hello")
      blackhole.consume(cb.read())
    }
  }

  @Benchmark
  def readViaIndex(blackhole: Blackhole): Unit = {
    loop.foreach { i =>
      cb.write("hello")
      blackhole.consume(cb(i))
    }
  }
}
