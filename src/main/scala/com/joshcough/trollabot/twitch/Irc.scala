package com.joshcough.trollabot.twitch

import cats.Show
import cats.effect.kernel.Async
import cats.implicits._
import com.comcast.ip4s._
import com.joshcough.trollabot.{ChannelName, ChatUser, ChatUserName}
import fs2.io.net.tls.TLSSocket
import fs2.{INothing, Pipe, Stream, text}
import fs2.io.net.{Network, Socket}
import io.circe.Encoder
import io.circe.generic.semiauto._
import logstage.strict.LogIOStrict

import scala.util.matching.Regex

case class IrcConfig(token: String, username: String, server: String, port: Port)

object Message {
  implicit val messageEnc: Encoder[Message] = deriveEncoder
  implicit val messageShow: Show[Message] = m => messageEnc(m).noSpaces
}
sealed trait Message { val text: String }
case class IncomingMessage(text: String, responses: List[OutgoingMessage]) extends Message
case class OutgoingMessage(text: String) extends Message
object OutgoingMessage {
  implicit val messageEnc: Encoder[OutgoingMessage] = deriveEncoder
}

object Irc {

  private val pong: OutgoingMessage = OutgoingMessage("PONG")
  private def capReq(capability: String): OutgoingMessage = OutgoingMessage(s"CAP REQ $capability")
  private def pass(token: String): OutgoingMessage = OutgoingMessage(s"PASS $token")
  private def nick(username: String): OutgoingMessage = OutgoingMessage(s"NICK $username")
  def join(channel: ChannelName): OutgoingMessage = OutgoingMessage(s"JOIN #${channel.name}")
  def part(channel: ChannelName): OutgoingMessage = OutgoingMessage(s"PART #${channel.name}")
  def privMsg(channel: ChannelName, message: String): OutgoingMessage =
    OutgoingMessage(s"PRIVMSG #${channel.name} :$message")

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

case class Irc[F[_]: Network: Async](ircConfig: IrcConfig, initialMessages: Stream[F, OutgoingMessage])(
    processChatMessage: ChatMessage => Stream[F, OutgoingMessage]
)(implicit L: LogIOStrict[F]) {
  val stream: fs2.Stream[F, Message] = {

    def outgoingBytesPipe: Pipe[F, OutgoingMessage, Byte] =
      _.map(m => s"${m.text}\n").through(text.utf8.encode)
    def writeToSocketPipe(s: Socket[F]): Pipe[F, OutgoingMessage, INothing] =
      _.through(outgoingBytesPipe).through(s.writes)

    def sendMessage(socket: Socket[F], message: OutgoingMessage): Stream[F, OutgoingMessage] =
      Stream[F, OutgoingMessage](message).observe(writeToSocketPipe(socket))

    def handleIncomingMessage(socket: Socket[F], m: String): Stream[F, IncomingMessage] =
      m.trim match {
        case command if command.startsWith("PING") => sendMessage(socket, pong) *> Stream()
        case _ @PRIVMSGRegex(badges, username, _, channel, message) =>
          val cm = createChatMessage(badges, username, channel, message)
          processChatMessage(cm).flatMap(om => sendMessage(socket, om).map(_ => IncomingMessage(m, List(om))))
        case x => Stream(IncomingMessage(x, Nil))
      }

    val loginStream: Stream[F, OutgoingMessage] = Stream(login(ircConfig): _*)

    withSocket { socket =>
      val outgoing: Stream[F, OutgoingMessage] = (loginStream ++ initialMessages).flatMap(sendMessage(socket, _))
      val incoming: Stream[F, IncomingMessage] = socket.reads.through(text.utf8.decode).flatMap { s =>
        for {
          _ <- Stream.eval(L.debug(s"handling incoming message: $s"))
          m <- handleIncomingMessage(socket, s)
        } yield m
      }
      outgoing ++ incoming.repeat
    }
  }

  def withSocket[A](f: TLSSocket[F] => Stream[F, A]): Stream[F, A] = {
    val addr: SocketAddress[Host] = Host
      .fromString(ircConfig.server)
      .map(SocketAddress(_, ircConfig.port))
      .getOrElse(throw new RuntimeException("couldn't read server from config"))

    for {
      _ <- Stream.eval(L.debug(s"Connecting to socket"))
      socket <- Stream.resource(Network[F].client(addr))
      tlsContext <- Stream.eval(fs2.io.net.tls.TLSContext.Builder.forAsync[F].system)
      tlsSocket <- Stream.resource(tlsContext.client(socket))
      _ <- Stream.eval(L.debug(s"Connected to socket"))
      res <- f(tlsSocket)
    } yield res
  }
}
