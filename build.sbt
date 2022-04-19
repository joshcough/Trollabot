lazy val root = (project in file("."))
  .settings(
    name := "trollabot-scala",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    assembly / mainClass := Some("App")
  )

resolvers += Resolver.JCenterRepository

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.3.4", //org.postgresql.ds.PGSimpleDataSource dependency
  "org.scalatest" %% "scalatest" % "3.2.6" % Test,
  "com.typesafe" % "config" % "1.4.2",
  "net.katsstuff" %% "ackcord"                 % "0.17.1", //For high level API, includes all the other modules
)

scalacOptions += "-deprecation"

run / fork := true

//enablePlugins(DockerPlugin)
//
//docker / dockerfile := {
//  // The assembly task generates a fat JAR file
//  val artifact: File = assembly.value
//  val artifactTargetPath = s"/app/${artifact.name}"
//
//  def lookup(v: String): (String, String) = v -> sys.env(v)
//
//  new Dockerfile {
//    from("openjdk:8-jre")
//    arg("TROLLABOT_TOKEN")
//    env(lookup("TROLLABOT_TOKEN"))
//    add(artifact, artifactTargetPath)
//    entryPoint("java", "-jar", artifactTargetPath)
//  }
//}
//
//docker / buildOptions := BuildOptions(cache = false)
