lazy val root = (project in file("."))
  .settings(
    name := "trollabot-scala",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    assembly / mainClass := Some("App")
  )

lazy val doobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"       % "3.2.6" % Test,
  "com.typesafe"  %  "config"          % "1.4.2",
  "org.tpolecat"  %% "doobie-core"     % doobieVersion,
  "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
  "org.tpolecat"  %% "doobie-specs2"   % doobieVersion
)

scalacOptions += "-deprecation"

run / fork := true
