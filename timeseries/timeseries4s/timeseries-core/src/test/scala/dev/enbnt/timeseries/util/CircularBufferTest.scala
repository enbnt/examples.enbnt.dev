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
      rb.readIndex(1)
    }
    rb.write(2)
    intercept[IllegalStateException] {
      rb.readRaw(2)
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

  test("CircularBuffer#writeIndex") {
    val rb = new CircularBuffer[Int](4)
    assert(rb.writeIndex() == 0)
    rb.write(1, 2)
    assert(rb.writeIndex() == 2)
    rb.write(3, 4)
    assert(rb.writeIndex() == 0)
    assert(rb.writeIndex(2) == 2)
    assert(rb.writeIndex(4) == 0)
    rb.write(5, 6)
    assert(rb.writeIndex() == 2)
    assert(rb.writeIndex(-4) == 2)
    rb.write(7, 8)
    assert(rb.writeIndex() == 0)
    assert(rb.writeIndex(-4) == 0)
  }

  test("CircularBuffer#readIndex") {
    val rb = new CircularBuffer[Int](4)
    rb.write(1, 2, 3, 4)
    assert(rb.readIndex() == 0)
    rb.read()
    assert(rb.readIndex() == 1)
    assert(rb.readIndex(2) == 3)
    assert(rb.readIndex() == 1)
    rb.read()
    assert(rb.readIndex() == 2)
    assert(rb.readIndex(-4) == 2)
    assert(rb.readIndex(4) == 2)
    assert(rb.readIndex(2) == 0)
    assert(rb.readIndex(-2) == 0)
    rb.read()
    rb.read()
    assert(rb.readIndex() == 0)
    assert(rb.readIndex(3) == 3)
    assert(rb.readIndex(4) == 0)
    assert(rb.writeIndex() == 0)
  }

  test("CircularBuffer#readRaw") {
    val rb = new CircularBuffer[Int](4)
    rb.write(1, 2, 3, 4)
    assert(rb.readRaw(3) == 4)
    assert(rb.readRaw(0) == 1)
    rb.write(5, 6)
    assert(rb.readRaw(3) == 4)
    assert(rb.readRaw(0) == 5)
    assert(rb.readRaw(1) == 6)
    assert(rb.read() == 5) // verify that we haven't modified the read index
  }

  test("CircularBuffer#iterator") {
    val rb = new CircularBuffer[Int](4)
    assert(rb.iterator == Iterator.empty[Int])
    assert(rb.isEmpty)

    rb.write(1, 2, 3, 4)
    assert(rb.nonEmpty)
    rb.to[LazyList[Int]](LazyList.iterableFactory) shouldEqual Seq(1, 2, 3, 4)
  }

}
