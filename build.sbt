name := "wth-pons"

version := "0.0.1-SNAPSHOT"

organization in ThisBuild := "io.funkyminds"

scalaVersion := "2.13.4"

scalacOptions := Seq("-unchecked", "-deprecation")

val zioVersion = "1.0.3"

//@formatter:off
libraryDependencies ++= Seq(
  "io.funkyminds"       %%  "wth-core"            % "0.0.1-SNAPSHOT",
  "dev.zio"             %%  "zio"                 % zioVersion,
  "dev.zio"             %%  "zio-streams"         % zioVersion,
  "dev.zio"             %% "zio-test"             % zioVersion % "test",
  "dev.zio"             %% "zio-test-sbt"         % zioVersion % "test"
)
//@formatter:on

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
