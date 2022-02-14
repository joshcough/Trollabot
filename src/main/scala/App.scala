import java.io._
import java.net.SocketException
import javax.net.ssl.{SSLSocket, SSLSocketFactory}
import slick.jdbc.PostgresProfile.api._
import TrollabotDb.getRandomQuote_

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.matching.Regex

object App {

  val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault.asInstanceOf[SSLSocketFactory]

  val socket: SSLSocket = socketFactory
    .createSocket(Configuration.ircServer, Configuration.ircPort)
    .asInstanceOf[SSLSocket]
  socket.startHandshake()

  val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
  val writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream)))

  val db = Database.forConfig("db")

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      socket.close()
      println("Bye!")
    }
  })

  val PRIVMSGRegex: Regex =
    """^(@\S+ )?:(\S+)!(\S+)? PRIVMSG #(\S+) :(.+)$""".r // badges, username, _, channel, message

  def pong(): Unit = send("PONG")
  def capReq(capabilities: Option[String]): Unit = capabilities.foreach { cap => send(s"CAP REQ $cap") }
  def pass(token: String): Unit = send(s"PASS $token")
  def nick(username: String): Unit = send(s"NICK $username")
  def join(channel: String): Unit = send(s"JOIN #$channel")
  def privMsg(channel: String, message: String): Unit = send(s"PRIVMSG #$channel :$message")

  def send(s: String): Unit = {
    writer.println(s)
    writer.flush()
    if (Configuration.debug) println(s"< $s")
  }

  def messageIs(message: String, s: String): Boolean = message.trim.equalsIgnoreCase(s)

  def processMessage(message: String): Unit = {
    print(s"message: $message")
    message match {
      case command if command.startsWith("PING") => pong()
      case PRIVMSGRegex(_, username, _, channel, message) if messageIs(message, "!hello") =>
        privMsg(channel, s"Hello $username")
      case PRIVMSGRegex(_, _, _, channel, message) if messageIs(message, "!quote") =>
        val quote: Option[Quote] = Await.result(db.run(getRandomQuote_.result), 1.second).headOption
        quote match {
          case Some(q) => privMsg(channel, s"Quote #${q.qid}: ${q.text}")
          case None => privMsg(channel, s"I couldn't find any quotes! grassSad")
        }
      case _ =>
    }
  }

  def main(args: Array[String]): Unit = {
    capReq(Configuration.ircCapabilities)
    pass(Configuration.ircToken)
    nick(Configuration.ircUsername)
    join(Configuration.ircChannel)

    try {
      Iterator
        .iterate(reader.readLine())(_ => reader.readLine())
        .takeWhile(_ != null)
        .map(_.trim)
        .filter(_.nonEmpty)
        .foreach(processMessage)
    } catch {
      case e: SocketException => println("Socket exception: " + e.getMessage)
    }
  }
}
