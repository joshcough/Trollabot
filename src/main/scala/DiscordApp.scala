import ackcord.{APIMessage, _}
import akka.NotUsed
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DiscordApp extends App {
  val token = "OTY1NzkyNDIxMzk5MTM0MzE4.Yl4WSA.092YfO6iCqA5gFE8D0VTAnaZJGI"
  val clientSettings = ClientSettings(token)
  val client = Await.result(clientSettings.createClient(), Duration.Inf)
  val myListeners = new MyListeners(client.requests)
  client.registerListener(myListeners.onLogin)
  client.login()
}

class MyListeners(requests: Requests) extends EventsController(requests) {
  val onLogin: EventListener[APIMessage.Ready, NotUsed] =
    Event.on[APIMessage.Ready].withSideEffects { _ => println("Logged in.") }
}
