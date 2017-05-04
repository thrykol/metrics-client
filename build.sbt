import Dependencies._

lazy val root = (project in file(".")).
  settings(
  ).aggregate(client)

lazy val client = (project in file("client")).
  settings(Commons.settings: _*).
  settings(
    name := "metrics-client",
    libraryDependencies ++= rootDependencies,
    libraryDependencies ++= testDependencies
  )
