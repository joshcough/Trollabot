import com.joshcough.trollabot.Chatbot
import slick.jdbc.PostgresProfile.api.Database

object App {
  def main(args: Array[String]): Unit = {
    val chatbot = Chatbot(Database.forConfig("db"))
    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = chatbot.close() })
    chatbot.run()
  }
}
