import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.joshcough.trollabot.{Configuration, TrollabotDb}
import com.joshcough.trollabot.twitch.Chatbot
import doobie.Transactor

object App {
  val mainAction: IO[Unit] = for {
    config <- Configuration.read()
    db: TrollabotDb[IO] = TrollabotDb(Transactor.fromDriverManager[IO]("org.postgresql.Driver", config.dbUrl))
    chatbot <- Chatbot(config, db)
    _ <- IO(Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        IO(chatbot.close()).unsafeRunSync()
        ()
      }
    }))
    _ <- chatbot.run()
  } yield ()

  def main(args: Array[String]): Unit = mainAction.unsafeRunSync()
}
