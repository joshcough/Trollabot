import cats.effect.IO
import com.joshcough.trollabot.{Chatbot, Configuration}
import doobie.Transactor

object App {
  def main(args: Array[String]): Unit = {
    val chatbot = Chatbot(Transactor.fromDriverManager[IO]("org.postgresql.Driver", Configuration.dbUrl))
    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = chatbot.close() })
    chatbot.run()
  }
}
