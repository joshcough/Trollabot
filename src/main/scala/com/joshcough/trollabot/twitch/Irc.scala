package com.joshcough.trollabot.twitch

import cats.effect.kernel.Concurrent
import cats.implicits._
import com.comcast.ip4s._
import com.joshcough.trollabot.IrcConfig
import fs2.{INothing, Pipe, Stream, text}
import fs2.io.net.{Network, Socket}
import logstage.strict.LogIOStrict

import scala.util.matching.Regex

trait Message { val text: String }
case class IncomingMessage(text: String, responses: List[OutgoingMessage]) extends Message
case class OutgoingMessage(text: String) extends Message

object Irc {

  private val pong: OutgoingMessage = OutgoingMessage("PONG")
  private def capReq(capability: String): OutgoingMessage = OutgoingMessage(s"CAP REQ $capability")
  private def pass(token: String): OutgoingMessage = OutgoingMessage(s"PASS $token")
  private def nick(username: String): OutgoingMessage = OutgoingMessage(s"NICK $username")
  def join(channel: String): OutgoingMessage = OutgoingMessage(s"JOIN #$channel")
  def part(channel: String): OutgoingMessage = OutgoingMessage(s"PART #$channel")
  def privMsg(channel: String, message: String): OutgoingMessage = OutgoingMessage(s"PRIVMSG #$channel :$message")

  val PRIVMSGRegex: Regex =
    """^(@\S+ )?:(\S+)!(\S+)? PRIVMSG #(\S+) :(.+)$""".r // badges, username, _, channel, message

  def login(ircConfig: IrcConfig): List[OutgoingMessage] =
    List(
      pass(ircConfig.token),
      nick(ircConfig.username),
      capReq("twitch.tv/membership"),
      capReq("twitch.tv/commands"),
      capReq("twitch.tv/tags")
    )

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
}

import Irc._

case class Irc[F[_]: Network: Concurrent]
  (ircConfig: IrcConfig, initialMessages: Stream[F, OutgoingMessage])(
    processChatMessage: ChatMessage => Stream[F, OutgoingMessage]
)(implicit L: LogIOStrict[F]) {
  val stream: fs2.Stream[F, Message] = {

    def outgoingBytesPipe: Pipe[F, OutgoingMessage, Byte] =
      _.map(m => s"${m.text}\n").through(text.utf8.encode)
    def writeToSocketPipe(s: Socket[F]): Pipe[F, OutgoingMessage, INothing] =
      _.through(outgoingBytesPipe).through(s.writes)

    def sendMessage(socket: Socket[F], message: OutgoingMessage): Stream[F, OutgoingMessage] =
      Stream.eval(L.debug(s"sending message: ${message.text}")) *>
      Stream[F, OutgoingMessage](message).observe(writeToSocketPipe(socket)) <*
      Stream.eval(L.debug(s"sent message: ${message.text}"))

    def handleIncomingMessage(socket: Socket[F], m: String): Stream[F, IncomingMessage] = m match {
      case command if command.startsWith("PING") =>
        sendMessage(socket, pong) *> Stream(IncomingMessage(command, List(pong)))
      case s@PRIVMSGRegex(badges, username, _, channel, message) =>
        val cm = createChatMessage(badges, username, channel, message)
        for {
          m <- processChatMessage(cm)
          _ <- sendMessage(socket, m)
        } yield IncomingMessage(s, List(m))
      case x => Stream(IncomingMessage(x, Nil))
    }

    val loginStream: Stream[F, OutgoingMessage] = Stream(login(ircConfig): _*)

    withSocket { socket =>
      val outGoing: Stream[F, OutgoingMessage] = (loginStream ++ initialMessages).flatMap(sendMessage(socket, _))
      val incoming: Stream[F, IncomingMessage] = socket.reads.through(text.utf8.decode).flatMap { s =>
        for {
          _ <- Stream.eval(L.debug(s"handling incoming message: $s"))
          m <- handleIncomingMessage(socket, s)
        } yield m
      }
      outGoing ++ incoming
    }
  }

  def withSocket[A](f: Socket[F] => Stream[F, A]): Stream[F, A] = {
    // todo: if we use pureconfig, we can have real types in the config and then remove this code
    val addr = (for {
      h <- Host.fromString(ircConfig.server)
      p <- Port.fromInt(ircConfig.port)
    } yield SocketAddress(h, p)).getOrElse(throw new RuntimeException("couldn't read server or port from config"))

    for {
      _      <- Stream.eval(L.debug(s"Connecting to socket"))
      socket <- Stream.resource(Network[F].client(addr))
      _      <- Stream.eval(L.debug(s"Connected to socket"))
      res    <- f(socket)
    } yield res
  }
}


//    def sendMessage2(s: Socket[F], message: OutgoingMessage): Stream[F, OutgoingMessage] = for {
//      _ <- Stream.eval(L.debug(s"sending message: ${message.text}"))
//      m <- Stream(message).observe()
//
//      Stream(s"${message.text}\n").
//
//        through(text.utf8.encode).through(s.writes).as(message)
////      m <- Stream(s"${message.text}\n").through(text.utf8.encode).observe()
//    } yield m
////    def observe(p: Pipe[F, O, INothing])(implicit F: Concurrent[F]): Stream[F, O] =


//    def logged: Pipe[F, OutgoingMessage, OutgoingMessage] = {
//      s: Stream[F, OutgoingMessage] => s.evalMap(m => L.debug(s"sending message: ${m.text}") *> m.pure[F])
//    }

//    def sendMessage(s: Socket[F], message: OutgoingMessage): Stream[F, OutgoingMessage] = for {
//      _ <- Stream.eval(L.debug(s"sending message: ${message.text}"))
//      m <- Stream(message).observe(writeToSocketPipe(s))
//      _ <- Stream.eval(L.debug(s"sent message: ${m.text}"))
//    } yield m
