package com.joshcough.trollabot.twitch

import cats.effect.IO
import cats.implicits._
import com.joshcough.trollabot.Configuration

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.SocketException
import javax.net.ssl.{SSLSocket, SSLSocketFactory}
import scala.util.matching.Regex

object Irc {

  def connectFromConfig(config: Configuration)(processChatMessage: (IrcBase, ChatMessage) => IO[Unit]): IO[Irc] =
    for {
      server <- IO(config.irc.server)
      port <- IO(config.irc.port)
      res <- connect(config, server, port)(processChatMessage)
    } yield res

  def connect(config: Configuration, server: String, port: Int)(
      processChatMessage: (IrcBase, ChatMessage) => IO[Unit]
  ): IO[Irc] =
    IO {
      val socket: SSLSocket =
        SSLSocketFactory.getDefault
          .asInstanceOf[SSLSocketFactory]
          .createSocket(server, port)
          .asInstanceOf[SSLSocket]
      socket.startHandshake()

      val reader: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val writer: PrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream)))
      val base: IrcBase = IrcBase(config, socket, reader, writer)

      new Irc(base, processChatMessage)
    }
}

case class IrcBase(config: Configuration, socket: SSLSocket, reader: BufferedReader, writer: PrintWriter) {

  val PRIVMSGRegex: Regex =
    """^(@\S+ )?:(\S+)!(\S+)? PRIVMSG #(\S+) :(.+)$""".r // badges, username, _, channel, message

  def pong(): IO[Unit] = send("PONG")
  def capReq(capability: String): IO[Unit] = send(s"CAP REQ $capability")
  def pass(token: String): IO[Unit] = send(s"PASS $token")
  def nick(username: String): IO[Unit] = send(s"NICK $username")
  def join(channel: String): IO[Unit] = send(s"JOIN #$channel")
  def part(channel: String): IO[Unit] = send(s"PART #$channel")
  def privMsg(channel: String, message: String): IO[Unit] = send(s"PRIVMSG #$channel :$message")

  def login(): IO[Unit] =
    for {
      _ <- pass(config.irc.token)
      _ <- nick(config.irc.username)
      _ <- capReq("twitch.tv/membership")
      _ <- capReq("twitch.tv/commands")
      _ <- capReq("twitch.tv/tags")
    } yield ()

  def send(s: String): IO[Unit] =
    IO {
      writer.println(s)
      writer.flush()
      if (true) println(s"sending: $s")
    }

  def close(): IO[Unit] =
    IO {
      socket.close()
      println("Trollabot shutting down!")
    }
}

case class Irc(base: IrcBase, processChatMessage: (IrcBase, ChatMessage) => IO[Unit]) {

  val PRIVMSGRegex: Regex =
    """^(@\S+ )?:(\S+)!(\S+)? PRIVMSG #(\S+) :(.+)$""".r // badges, username, _, channel, message

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
    def is(fld: String): Boolean = badgeMap.get(fld).contains("1")
    val chatUser = ChatUser(chatUserName, is("mod"), is("subscriber"), badgeMap)
    ChatMessage(chatUser, ChannelName(channel), message)
  }

  def processIrcMessage(message: String): IO[Unit] = {
    println(s"processing incoming message: $message")
    message match {
      case command if command.startsWith("PING") => base.pong()
      case PRIVMSGRegex(badges, username, _, channel, message) =>
        processChatMessage(base, createChatMessage(badges, username, channel, message))
      case _ => ().pure[IO]
    }
  }

  def processMessages(): IO[Unit] =
    try {
      import cats.implicits._
      Iterator
        .iterate(base.reader.readLine())(_ => base.reader.readLine())
        .takeWhile(_ != null)
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(processIrcMessage)
        .toList
        .sequence_
    } catch {
      case e: SocketException => IO(println("Socket exception: " + e.getMessage))
    }
}

//  def messageMatches(message: String, s: String): Boolean = message.trim.equalsIgnoreCase(s)
