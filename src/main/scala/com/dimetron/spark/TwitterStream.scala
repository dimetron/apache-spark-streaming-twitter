package com.dimetron.spark

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Duration, Minutes, Seconds, StreamingContext}
import StreamingContext._
import org.apache.spark.{SparkContext, SparkConf}

import scala.io.Source

object TwitterStream extends App {

  @transient var sc: SparkContext = _

  /**
   * Load twitter oauth keys for twitter4j client from twitter4j.properties
   * It should contain the following keys:
   *
   * twitter4j.oauth.consumerKey=..
   * twitter4j.oauth.consumerSecret=..
   * twitter4j.oauth.accessToken=..
   * twitter4j.oauth.accessTokenSecret=..
   *
   */
  def loadTwitterKeys() = {
    val lines: Iterator[String] = Source.fromFile("twitter4j.properties").getLines()
    val props = lines.map(line => line.split("=")).map { case (scala.Array(k, v)) => (k, v)}
    props.foreach {
      case (k: String, v: String) => System.setProperty(k, v)
    }
  }

  /**
   * Function to create streaming context and set refresh to 10 sec
   * @return
   */
  def configureStreamingContext() = {
    val conf = new SparkConf().setMaster("local[3]").setAppName("Twitter")
    sc = new SparkContext(conf)
    new StreamingContext(sc, Seconds(10))
  }

  /**
   * Start the stream using filter keywords and print reduced results every 10 seconds
   */
  def startStream() = {
    val duration: Duration = Seconds(3600)
    val filters = Seq("Yeoman", "npm", "gruntjs", "jsconf", "nodejs", "docker", "tdd")

    val ssc = configureStreamingContext()
    val tweets = TwitterUtils.createStream(ssc, None, filters, StorageLevel.MEMORY_ONLY_SER_2)

    // Print tweets batch count
    tweets.foreachRDD(rdd => {
      println("\nNew tweets %s:".format(rdd.count()))
    })

    //get users and followers count
    val users = tweets.map(status =>
      (status.getUser().getScreenName(), status.getUser().getFollowersCount())
    )

    //print top users
    val usersReduced = users.reduceByKeyAndWindow(_ + _, duration).map { case (user, count) => (count, user)}.transform(_.sortByKey(false))
    usersReduced.foreachRDD(rdd => {
      println("ReducedUsersCount= %s ".format(rdd.count()))
      val topUsers = rdd.take(10)
      topUsers.foreach { case (count, user) => println("%s (%s followers)".format(user, count))}
    })

    // Print popular hash tags
    val hashTags = tweets.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))
    val topHashTags = hashTags.map((_, 1))
      .reduceByKeyAndWindow(_ + _, duration)
      .map { case (topic, count) => (count, topic)}
      .transform(_.sortByKey(false))

    topHashTags.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last %s seconds (%s total):".format(duration, rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    ssc.start()
    //ssc.awaitTermination(Minutes(300).milliseconds)
    ssc.awaitTermination()
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }

  override def main(args: Array[String]) {
    Logger.getRootLogger.setLevel(Level.ERROR)
    TwitterStream.loadTwitterKeys()
    TwitterStream.startStream()
  }
}
