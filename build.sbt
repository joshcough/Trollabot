import Commands._
import Deps._
import java.time.Instant

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .enablePlugins(BuildInfoPlugin, GitVersioning)
  .settings(
    name := "trollabot-scala",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    assembly / mainClass := Some("com.joshcough.trollabot.App"),
    buildInfoPackage := "com.joshcough.trollabot.raw",
    buildInfoKeys ++= {
      val t = Instant.now
      Seq[BuildInfoKey](
        name,
        version,
        scalaVersion,
        sbtVersion,
        BuildInfoKey.action("buildTime") { t },
        "commit" -> git.gitHeadCommit.value.map(_.take(7))
      )
    },
    Defaults.itSettings,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    IntegrationTest / fork := true,
    scalacOptions += "-deprecation",
    run / fork := true,
    resolvers += "mrdziuban-maven" at "https://raw.githubusercontent.com/mrdziuban/maven-repo/master",
    libraryDependencies ++= Deps.deps,
    testFrameworks += new TestFramework("munit.Framework")
  )

commands ++= Seq(format, formatCheck)
