import Dependencies._

lazy val root = (project in file(".")).
  settings(
  ).aggregate(client, demo)

lazy val client = (project in file("client")).
  settings(Commons.settings: _*).
  settings(
    name := "metrics-client",
    libraryDependencies ++= rootDependencies,
    libraryDependencies ++= testDependencies,
    publishArtifact in Test := true,
    mappings in (Test, packageBin) ~= { _.filter(_._2.matches(".*\\/metrics\\/test\\/.*"))},
    mappings in (Test, packageSrc) ~= { _.filter(_._2.matches(".*\\/metrics\\/test\\/.*"))},
    mappings in (Test, packageDoc) ~= { _.filter(_._2.matches(".*\\/metrics\\/test\\/.*"))}
  )

lazy val demo = (project in file("demo")).
  settings(Commons.settings: _*).
  settings(
    name := "metrics-client-demo",
    libraryDependencies ++=  Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
    ),
    mainClass in (Compile,run) := Some("us.my_family.metrics.demo.RunDemo")
  ).
  dependsOn(client)
