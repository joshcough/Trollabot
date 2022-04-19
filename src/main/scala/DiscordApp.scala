import ackcord._
import ackcord.data._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DiscordApp extends App {

  val token = "TRoflymJ544dkEfSejsq7bGQNFjJbRFL" //Your Discord token. Be very careful to never give this to anyone else

  val clientSettings = ClientSettings(token)
  //The client settings contains an excecution context that you can use before you have access to the client
  import clientSettings.executionContext

  //In real code, please dont block on the client construction
  val client = Await.result(clientSettings.createClient(), Duration.Inf)

  //The client also contains an execution context
  import client.executionContext

  client.onEventSideEffectsIgnore {
    case APIMessage.Ready(_) => println("Now ready")
    case x@_ => print(s"x?: $x")
  }

  client.login()
  println("Logged in?")
}