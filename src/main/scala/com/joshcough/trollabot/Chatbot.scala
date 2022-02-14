package com.joshcough.trollabot

import java.io._
import java.net.SocketException
import javax.net.ssl.{SSLSocket, SSLSocketFactory}
import slick.jdbc.PostgresProfile.api._

import scala.util.matching.Regex

case class Chatbot(db: Database) {

  // Start Constructor stuff
  val socket: SSLSocket =
    SSLSocketFactory.getDefault.asInstanceOf[SSLSocketFactory]
      .createSocket(Configuration.ircServer, Configuration.ircPort)
      .asInstanceOf[SSLSocket]
  socket.startHandshake()

  val reader: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
  val writer: PrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream)))
  val trollabotDb: TrollabotDb = TrollabotDb(db)
  val commands: Commands = Commands(trollabotDb)

  // End Constructor stuff

  val PRIVMSGRegex: Regex =
    """^(@\S+ )?:(\S+)!(\S+)? PRIVMSG #(\S+) :(.+)$""".r // badges, username, _, channel, message

  def pong(): Unit = send("PONG")
  def capReq(capability: String): Unit = send(s"CAP REQ $capability")
  def pass(token: String): Unit = send(s"PASS $token")
  def nick(username: String): Unit = send(s"NICK $username")
  def join(channel: String): Unit = send(s"JOIN #$channel")
  def part(channel: String): Unit = send(s"PART #$channel")
  def privMsg(channel: String, message: String): Unit = send(s"PRIVMSG #$channel :$message")

  def send(s: String): Unit = {
    writer.println(s)
    writer.flush()
    if (Configuration.debug) println(s"< $s")
  }

  def messageMatches(message: String, s: String): Boolean = message.trim.equalsIgnoreCase(s)

  def parseBadges(s: String): Map[String, String] = {
    def parseBadge(s: String): (String, String) = {
      val (key, value) = s.span(_ != '=')
      (key, value.drop(1))
    }
    s.split(';').map(parseBadge).toMap
  }

  def createChatMessage(badges: String, username: String, channel: String, message: String): ChatMessage = {
    val badgeMap: Map[String, String] = parseBadges(badges)
    val chatUserName = ChatUserName(username)
    val chatUser = ChatUser(
      chatUserName,
      badgeMap.get("mod").contains("1"),
      badgeMap.get("subscriber").contains("1"),
      badgeMap
    )
    ChatMessage(chatUser, ChannelName(channel), message)
  }

  def processMessage(message: String): Unit = {
    message match {
      case command if command.startsWith("PING") => pong()
      case PRIVMSGRegex(badges, username, _, channel, message) =>
        // println(s"message: $message")
        // println(s"badges: $badges, username: $username, channel: $channel, message: $message")
        val responses = commands.findAndRun(createChatMessage(badges, username, channel, message))
        responses.foreach {
          case RespondWith(s) => privMsg(channel, s)
          case Part => part(channel)
        }
      case _ =>
    }
  }

  def login(): Unit = {
    pass(Configuration.ircToken)
    nick(Configuration.ircUsername)
    capReq ("twitch.tv/membership")
    capReq ("twitch.tv/commands")
    capReq ("twitch.tv/tags")
  }

  def join(s:Stream): Unit = {
    join(s.name)
    privMsg(s.name, s"Hello!")
  }

  def processMessages(): Unit = {
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

  def run(): Unit = {
    login()
    trollabotDb.getJoinedStreamsIO().foreach(join)
    processMessages()
    println("Done processing messages, shutting down.")
  }

  def close(): Unit = {
    socket.close()
    println("Trollabot shutting down!")
  }
}
