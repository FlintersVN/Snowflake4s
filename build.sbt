name := "Snowflake4s"

version := "0.0.1-ALPHA"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.1.0",
  "net.codingwell" %% "scala-guice" % "4.1.0"
)

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)