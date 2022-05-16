lazy val doobieVersion = "1.0.0-RC1"

lazy val tcsVersion = "0.39.12"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "trollabot-scala",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    assembly / mainClass := Some("App"),
    Defaults.itSettings,
//    addCompilerPlugin("io.tryp" % "splain" % "0.5.8" cross CrossVersion.patch),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    IntegrationTest / fork := true,
    scalacOptions += "-deprecation",
    run / fork := true,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"       % "3.2.6" % Test,
      "com.typesafe"  %  "config"          % "1.4.2",
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-postgres" % doobieVersion,
      "org.tpolecat"  %% "doobie-specs2"   % doobieVersion,
      // test related, probably...
      "org.typelevel"             %% "kittens"                         % "2.3.2",
      "org.scalameta"             %% "svm-subs"                        % "20.2.0",
      "org.typelevel"             %% "scalacheck-effect-munit"         % "1.0.3"        % "test,it",
      "org.typelevel"             %% "munit-cats-effect-3"             % "1.0.0"        % "test,it",
      "com.dimafeng"              %% "testcontainers-scala-munit"      % tcsVersion     % "it",
      "com.dimafeng"              %% "testcontainers-scala-kafka"      % tcsVersion     % "it",
      "com.dimafeng"              %% "testcontainers-scala-postgresql" % tcsVersion     % "it",
    )
  )


