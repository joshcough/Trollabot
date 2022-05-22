package com.joshcough.trollabot.twitch

import cats.effect.kernel.Concurrent
import cats.implicits._
import com.joshcough.trollabot.{Quote, TrollabotDb}
import ParserCombinators._

trait Permission
case object God extends Permission
case object Owner extends Permission
case object ModOnly extends Permission
case object Anyone extends Permission

trait Response
case class RespondWith(s: String) extends Response
case class Join(newChannel: String) extends Response
case object Part extends Response

case class ChatUserName(name: String)
case class ChatUser(username: ChatUserName, isMod: Boolean, subscriber: Boolean, badges: Map[String, String])
case class ChannelName(name: String)

trait BotCommand[F[_]] {
  type A
  val name: String
  val permission: Permission
  val parser: Parser[A]
  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): fs2.Stream[F, Response]
  override def toString: String = s"Command(name: $name, perm: $permission)"
}

object BotCommand {
  def apply[F[_], R](
      commandName: String,
      perm: Permission,
      commandParser: Parser[R],
      f: (ChannelName, ChatUser, R) => fs2.Stream[F, Response]
  ): BotCommand[F] =
    new BotCommand[F] {
      override type A = R
      val name: String = commandName
      val permission: Permission = perm
      val parser: Parser[A] = commandParser
      def execute(channelName: ChannelName, chatUser: ChatUser, r: R): fs2.Stream[F, Response] =
        f(channelName, chatUser, r)
    }
}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case class Commands[F[_]: Concurrent](db: TrollabotDb[F]) {

  val printStreamsCommand: BotCommand[F] =
    BotCommand(
      "!printStreams",
      God,
      empty,
      (_, _, _: Unit) => db.getAllStreams.map(_.toString).reduce((l, r) => s"$l, $r").map(RespondWith)
    )

  def withQuoteOr(s: fs2.Stream[F, Quote], msg: String): fs2.Stream[F, Response] =
    fs2.Stream.eval(s.compile.last).map(q => RespondWith(q.map(_.display).getOrElse(msg)))

  val getQuoteCommand: BotCommand[F] = {
    BotCommand(
      "!quote",
      Anyone,
      int.?,
      (channelName, _, mn: Option[Int]) => {
        mn match {
          case None =>
            withQuoteOr(db.getRandomQuoteForStream(channelName.name), "I couldn't find any quotes, man.")
          case Some(n) =>
            withQuoteOr(db.getQuoteByQid(channelName.name, n), s"I couldn't find quote #$n, man.")
        }
      }
    )
  }

  val addQuoteCommand: BotCommand[F] =
    BotCommand(
      "!addQuote",
      ModOnly,
      slurp,
      (channelName, chatUser, text: String) =>
        withQuoteOr(
          db.insertQuote(text, chatUser.username.name, channelName.name),
          s"Something went wrong! I couldn't add stream ${channelName.name}. Somebody tell @artofthetroll"
        )
    )

  val delQuoteCommand: BotCommand[F] =
    BotCommand(
      "!delQuote",
      ModOnly,
      int,
      (channelName, _, n: Int) =>
        fs2.Stream.eval(db.deleteQuote(channelName.name, n).map {
          case 1 => RespondWith("Ok I deleted it.")
          case _ =>
            RespondWith(
              s"Something went wrong! I couldn't delete quote $n for channel ${channelName.name}. Somebody tell @artofthetroll"
            )
        })
    )

  val partCommand: BotCommand[F] =
    BotCommand(
      "!part",
      Owner,
      empty,
      (channelName, _, _: Unit) =>
        fs2.Stream
          .eval(db.partStream(channelName.name))
          .flatMap(_ =>
            fs2.Stream(
              RespondWith("Goodbye cruel world!"),
              Part
            )
          )
    )

  val joinCommand: BotCommand[F] = {
    BotCommand(
      "!join",
      God,
      anyString,
      (_, _, newChannelName: String) =>
        fs2.Stream
          .eval(for {
            b <- db.doesStreamExist(newChannelName).compile.last.map(_.getOrElse(false))
            z <- if (b) db.insertStream(newChannelName) else db.joinStream(newChannelName)
          } yield z)
          .flatMap(_ => fs2.Stream(Join(newChannelName), RespondWith(s"Joining $newChannelName!")))
    )
  }

  val commands: Map[String, BotCommand[F]] = List(
    joinCommand,
    partCommand,
    getQuoteCommand,
    addQuoteCommand,
    delQuoteCommand,
    printStreamsCommand
  ).map(c => (c.name, c)).toMap

  def findAndRun(msg: ChatMessage): fs2.Stream[F, Response] = {
    val (name, rest) = msg.body.trim.span(_ != ' ')
    commands.get(name) match {
      case Some(command) =>
        println(s"command: $command")
        runBotCommand(command, msg, rest)
      case None => fs2.Stream()
    }
  }

  def runBotCommand(cmd: BotCommand[F], msg: ChatMessage, args: String): fs2.Stream[F, Response] = {
    def go: fs2.Stream[F, Response] = {
      cmd.parser.apply(args.trim) match {
        case Success(r, _) => cmd.execute(msg.channel, msg.user, r)
        case Failure(_)    => fs2.Stream(RespondWith("Sorry, I don't understand that."))
      }
    }

    cmd.permission match {
      case God if isGod(msg.user)                                      => go
      case Owner if isStreamerOrGod(msg.user, msg)                     => go
      case ModOnly if msg.user.isMod || isStreamerOrGod(msg.user, msg) => go
      case Anyone                                                      => go
      case _                                                           => fs2.Stream()
    }
  }

  def isStreamerOrGod(chatUser: ChatUser, chatMessage: ChatMessage): Boolean =
    chatUser.username.name.toLowerCase == chatMessage.channel.name.toLowerCase || isGod(chatUser)

  def isGod(chatUser: ChatUser): Boolean = chatUser.username.name.toLowerCase == "artofthetroll"
}
