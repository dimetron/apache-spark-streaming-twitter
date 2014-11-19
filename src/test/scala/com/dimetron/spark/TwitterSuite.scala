package com.dimetron.spark

import org.apache.log4j.{Level, Logger}

import org.scalatest.{BeforeAndAfterAll, FunSuite}

class TwitterSuite extends FunSuite with BeforeAndAfterAll {


  override def beforeAll() {
    Logger.getRootLogger.setLevel(Level.ERROR)
    TwitterStream.loadTwitterKeys()
    super.beforeAll()
  }

  test("Stream example") {
    TwitterStream.startStream()
  }
}