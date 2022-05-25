import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.joshcough.trollabot.{Chatbot, Configuration}

object App {
  val mainAction: IO[Unit] = for {
    config <- Configuration.read()
    chatbot <- Chatbot(config)
    _ <- IO(Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = chatbot.close().unsafeRunSync()
    }))
    _ <- chatbot.run()
  } yield ()

  def main(args: Array[String]): Unit = mainAction.unsafeRunSync()
}
