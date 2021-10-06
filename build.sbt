ThisBuild / crossScalaVersions := List("2.12.14", "2.13.6")

inThisBuild(
  List(
    organization := "io.github.flintersvn",
    homepage := Some(url("https://github.com/FlintersVN")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "tung_nt",
        "Nguyen Thanh Tung",
        "tung_nt@septeni-technology.jp",
        url("https://github.com/NgTung")
      ),
      Developer(
        "huy_ngq",
        "Nguyen Quang Huy",
        "qhquanghuy96@gmail.com",
        url("https://github.com/qhquanghuy")
      ),
      Developer(
        "bang_nh",
        "Nguyen Huu Bang",
        "bangnh24@gmail.com",
        url("https://github.com/bangnh1")
      )
    )
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "Snowflake4s",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.google.inject" % "guice" % "4.1.0",
      "net.codingwell" %% "scala-guice" % "4.2.9",
      "com.github.tototoshi" % "scala-base62_2.10" % "0.1.0",
      "org.scalatest" %% "scalatest" % "3.2.10" % Test
    )
  )
  .settings(
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeProfileName := "io.github.flintersvn"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

Global / scalacOptions ++= Seq(
  "-Xlint:unused"
)
