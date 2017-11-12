sonatypeProfileName := "Snowflake4s"
organization := "com.septech"
publishMavenStyle := true

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("http://septeni-technology.jp/vn/"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/NgTung"),
    "scm:git@github.com:NgTung/snowflake4s.git"
  )
)
developers := List(
  Developer(
    id = "1",
    name = "TungNT",
    email = "tung_nt@septeni-technology.jp",
    url = url("http://septeni-technology.jp/vn/")
  )
)