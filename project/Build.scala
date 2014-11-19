import sbt._
import sbt.Keys._
import scala._

object BuildSettings {

  import Dependencies._

  lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
    name := "spark-practice",
    version := "0.1",
    organization := "com.dbtsai",
    scalaVersion := "2.10.4",
    exportJars := true,
    crossPaths := false,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Cloudera Releases" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    libraryDependencies ++= sharedLibraryDependencies
  )
}

object SparkPractice extends Build {

  import BuildSettings._

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = sharedSettings ++ Seq(
      name := "root"
    )
  )
}

object Dependencies {
  lazy val sharedLibraryDependencies = Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "org.apache.spark" % "spark-core_2.10" % "1.1.0",
    "org.apache.spark" % "spark-mllib_2.10" % "1.1.0",
    "org.apache.spark" % "spark-sql_2.10" % "1.1.0",
    "org.apache.spark" % "spark-hive_2.10" % "1.1.0",
    "org.apache.spark" % "spark-streaming-twitter_2.10" % "1.1.0"
  )
}
