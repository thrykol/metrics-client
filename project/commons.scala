import sbt._
import Keys._

object Commons {
  val appVersion = "0.1.0-SNAPSHOT"

  val settings = Seq(
    version := appVersion,
    scalaVersion := "2.11.8",
    organization := "us.my_family"
  )
}
