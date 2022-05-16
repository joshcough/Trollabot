import sbt.Command

object Commands {

  val format = Command.command("format") {
    "scalafmtSbt;" :: "scalafmtAll;" :: _
  }

  val formatCheck = Command.command("formatCheck") {
    "scalafmtSbtCheck;" :: "scalafmtCheckAll;" :: _
  }

}
