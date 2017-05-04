import sbt._
import Keys._

object Dependencies {

  val rootDependencies = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.2",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  )

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.3" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
  )
}
