import Commands._

val DoobieVersion = "1.0.0-RC1"
val TcsVersion = "0.39.12"
val Http4sVersion = "0.23.11"
val CirceVersion = "0.14.1"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.10"
val MunitCatsEffectVersion = "1.0.7"
val LogStageVersion = "1.0.10"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .enablePlugins(BuildInfoPlugin, GitVersioning)
  .settings(
    name := "trollabot-scala",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    assembly / mainClass := Some("com.joshcough.trollabot.twitch.App"),
    buildInfoPackage := "com.joshcough.trollabot",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoKeys ++= Seq[BuildInfoKey](
      BuildInfoKey.action("buildTime") { System.currentTimeMillis },
      "commit" -> git.gitHeadCommit.value.get.take(7)
    ),
    Defaults.itSettings,
//    addCompilerPlugin("io.tryp" % "splain" % "0.5.8" cross CrossVersion.patch),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    IntegrationTest / fork := true,
    scalacOptions += "-deprecation",
    run / fork := true,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.6" % Test,
      "com.typesafe" % "config" % "1.4.2",
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat" %% "doobie-munit" % DoobieVersion,
      // test related, probably...
      "org.typelevel" %% "kittens" % "2.3.2",
      "org.scalameta" %% "svm-subs" % "20.2.0",
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.3" % "test,it",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.0" % "test,it",
      "com.dimafeng" %% "testcontainers-scala-munit" % TcsVersion % "it",
      "com.dimafeng" %% "testcontainers-scala-kafka" % TcsVersion % "it",
      "com.dimafeng" %% "testcontainers-scala-postgresql" % TcsVersion % "it",
      // http4s
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
      "io.7mind.izumi" %% "logstage-core" % LogStageVersion,
      "io.7mind.izumi" %% "logstage-rendering-circe" % LogStageVersion
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

commands ++= Seq(format, formatCheck)

//al ingressApiDeps = List(
//  "org.typelevel"         %% "cats-effect"              %
//    IngressApi.CatsEffectVersion,
//  "com.github.pureconfig" %% "pureconfig-http4s"        % PureConfigVersion,
//  "io.circe"              %% "circe-refined"            % CirceVersion,
//  "io.github.jmcardon"    %% "tsec-jwt-sig"             %
//    IngressApi.TsecVersion,
//  "io.7mind.izumi"        %% "logstage-core"            % IngressApi.LogStage,
//  "io.7mind.izumi"        %% "logstage-rendering-circe" % IngressApi.LogStage,
//  "org.scalameta"         %% "munit"                    %
//    IngressApi.MunitVersion           % "test, it",
//  "org.typelevel"         %% "munit-cats-effect-3"      %
//    IngressApi.MunitCatsEffectVersion % "test, it"
//)
