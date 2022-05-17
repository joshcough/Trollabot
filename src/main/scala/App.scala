import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.joshcough.trollabot.{Chatbot, Configuration}
import doobie.Transactor

object App {
  val mainAction: IO[Unit] = for {
    dbUrl <- IO(Configuration.dbUrl)
    chatbot <- Chatbot(Transactor.fromDriverManager[IO]("org.postgresql.Driver", dbUrl))
    _ <- IO(Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = IO(chatbot.close()).unsafeRunSync()
    }))
    _ <- chatbot.run()
  } yield ()

  def main(args: Array[String]): Unit = mainAction.unsafeRunSync()
}
