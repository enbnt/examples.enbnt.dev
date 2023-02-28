package dev.enbnt.timeseries.util

import com.twitter.inject.Test
import scala.collection.IterableOnce.iterableOnceExtensionMethods

class CircularBufferTest extends Test {

  test("CircularBuffer#can read and write") {
    val rb = new CircularBuffer[Int](4)
    rb.write(5)
    val v = rb.read()
    assert(v == 5)

    rb.write(6)
    val v2 = rb.read()
    assert(v2 == 6)

    rb.write(7)
    rb.write(8)

    assert(rb.read() == 7)
    assert(rb.read() == 8)
  }

  test("CircularBuffer#read ahead of writes throws") {
    val rb = new CircularBuffer[Int](4)
    intercept[IllegalStateException] {
      rb.read()
    }
    rb.write(1)
    intercept[IllegalStateException] {
      rb.read(1)
    }
    rb.write(2)
    intercept[IllegalStateException] {
      rb.read(2)
    }
  }

  test("CircularBuffer#read and writes wrap around") {
    val rb = new CircularBuffer[Int](4)
    rb.write(1)
    rb.write(2)
    rb.write(3)
    rb.write(4)

    // buffer is full

    assert(rb.read() == 1)
    assert(rb.read() == 2)
    assert(rb.read() == 3)
    assert(rb.read() == 4)

    // buffer should wrap

    rb.write(5)
    rb.write(6)
    rb.write(7)
    rb.write(8)

    assert(rb.read() == 5)
    assert(rb.read() == 6)
    assert(rb.read() == 7)
    assert(rb.read() == 8)
  }

  test("CircularBuffer#read offset behaves as expected") {
    val rb = new CircularBuffer[Int](4)
    rb.write(1, 2, 3, 4)
    assert(rb.size == 4)
    assert(rb.nonEmpty)
    assert(rb.read(2) == 3)
    assert(rb.read() == 1)
    assert(rb.read(2) == 4)
    assert(rb.read() == 2)
    assert(rb.size == 2)
    intercept[IllegalStateException] {
      rb.read(2)
    }

    assert(rb.size == 2)
    assert(rb.read(0) == 3)
    assert(rb.read(1) == 4)

    assert(rb.read() == 3)

    intercept[IllegalStateException] {
      rb.read(2)
    }

    assert(rb.size == 1)
    intercept[IllegalStateException] {
      rb.read(1)
    }

  }

  test("CircularBuffer#iterator does not modify the read index") {
    val rb = new CircularBuffer[Int](4)
    assert(rb.iterator.isEmpty)
    assert(rb.isEmpty)

    rb.write(1, 2, 3, 4)
    assert(rb.nonEmpty)
    rb.to[LazyList[Int]](LazyList.iterableFactory) shouldEqual Seq(1, 2, 3, 4)
    assert(
      rb.nonEmpty
    ) // does not modify the read index, we can retrieve the same iterator
    rb.to[LazyList[Int]](LazyList.iterableFactory) shouldEqual Seq(1, 2, 3, 4)
  }

  test("CircularBuffer#apply does not modify the read index") {
    val rb = new CircularBuffer[String](8)
    assert(rb.isEmpty)
    rb.write("hello", "there", "good", "friend")
    assert(rb(0) == "hello")
    assert(rb(1) == "there")
    assert(rb(2) == "good")
    assert(rb(3) == "friend")
    intercept[IllegalStateException] {
      rb(4)
    }
  }

  test("CircularBuffer#drop") {
    val rb = new CircularBuffer[Int](4)
    assert(rb.iterator.isEmpty)
    assert(rb.isEmpty)

    rb.write(1, 2, 3, 4)
    assert(rb.nonEmpty)
    rb.drop(1)
      .take(2)
      .to[LazyList[Int]](LazyList.iterableFactory) shouldEqual Seq(2, 3)
  }

}
