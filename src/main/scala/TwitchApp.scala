import com.joshcough.trollabot.{Chatbot, Configuration}
import slick.jdbc.PostgresProfile.api.Database

object TwitchApp {
  def main(args: Array[String]): Unit = {
    val chatbot = Chatbot(Database.forURL(Configuration.dbUrl, driver = "org.postgresql.Driver"))
    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = chatbot.close() })
    chatbot.run()
  }
}
