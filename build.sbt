import Dependencies._

lazy val root = (project in file(".")).
  settings(
  ).aggregate(client)

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

