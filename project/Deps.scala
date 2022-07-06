import sbt._

object Deps {
  val DoobieVersion = "1.0.0-RC1"
  val TcsVersion = "0.39.12"
  val Http4sVersion = "0.23.11"
  val CirceVersion = "0.14.1"
  val MunitVersion = "0.7.29"
  val LogbackVersion = "1.2.10"
  val MunitCatsEffectVersion = "1.0.7"
  val LogStageVersion = "1.0.10"
  val CatsEffectVersion = "3.3.11"
  val PureConfigVersion = "0.17.1"
  val EnumeratumCirceVersion = "1.7.0"
  val RefinedVersion = "0.9.29"

  val deps = Seq(
    "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
    "com.beachape" %% "enumeratum-circe" % EnumeratumCirceVersion,
    "com.dimafeng" %% "testcontainers-scala-kafka" % TcsVersion % "it",
    "com.dimafeng" %% "testcontainers-scala-munit" % TcsVersion % "it",
    "com.dimafeng" %% "testcontainers-scala-postgresql" % TcsVersion % "it",
    "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-http4s" % PureConfigVersion,
    "eu.timepit" %% "refined" % RefinedVersion,
    "eu.timepit" %% "refined-pureconfig" % RefinedVersion,
    "io.7mind.izumi" %% "logstage-core" % LogStageVersion,
    "io.7mind.izumi" %% "logstage-rendering-circe" % LogStageVersion,
    "io.circe" %% "circe-core" % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser" % CirceVersion,
    "io.circe" %% "circe-shapes" % CirceVersion,
    "io.circe" %% "circe-refined" % CirceVersion,
    "org.http4s" %% "http4s-circe" % Http4sVersion,
    "org.http4s" %% "http4s-dsl" % Http4sVersion,
    "org.http4s" %% "http4s-ember-server" % Http4sVersion,
    "org.http4s" %% "http4s-ember-client" % Http4sVersion,
    // TODO: i feel like i'll need this one soon so leaving it here
    //"org.http4s"            %% "http4s-prometheus-metrics" % Http4sVersion,
    "org.scalameta" %% "svm-subs" % "20.2.0",
    "org.scalameta" %% "munit" % MunitVersion % "test,it",
    "org.scalatest" %% "scalatest" % "3.2.6" % Test,
    "org.tpolecat" %% "doobie-core" % DoobieVersion,
    "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
    "org.tpolecat" %% "doobie-munit" % DoobieVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    "org.typelevel" %% "cats-laws" % "2.7.0" % "test,it",
    "org.typelevel" %% "kittens" % "2.3.2",
    "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % "test,it",
    "org.typelevel" %% "scalacheck-effect-munit" % "1.0.3" % "test,it"
  )
}
