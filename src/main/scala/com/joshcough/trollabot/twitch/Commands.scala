package com.joshcough.trollabot.twitch

import cats.effect.IO
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

trait BotCommand {
  type A
  val name: String
  val permission: Permission
  val parser: Parser[A]
  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): IO[List[Response]]
  override def toString: String = s"Command(name: $name, perm: $permission)"
}

object BotCommand {
  def apply[R](
      commandName: String,
      perm: Permission,
      commandParser: Parser[R],
      f: (ChannelName, ChatUser, R) => IO[List[Response]]
  ): BotCommand = {
    new BotCommand {
      override type A = R
      val name: String = commandName
      val permission: Permission = perm
      val parser: Parser[A] = commandParser
      def execute(channelName: ChannelName, chatUser: ChatUser, r: A): IO[List[Response]] =
        f(channelName, chatUser, r)
    }
  }
}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case class Commands(db: TrollabotDb[IO]) {

  val printStreamsCommand: BotCommand =
    BotCommand(
      "!printStreams",
      God,
      empty,
      (_, _, _: Unit) =>
        for {
          streams <- db.getAllStreams
        } yield List(RespondWith(streams.map(_.toString).mkString(", ")))
    )

  val getQuoteCommand: BotCommand = {
    def f(msg: String, mq: Option[Quote]) = List(RespondWith(mq.map(_.display).getOrElse(msg)))
    BotCommand(
      "!quote",
      Anyone,
      int.?,
      (channelName, _, mn: Option[Int]) => {
        mn match {
          case None    => db.getRandomQuoteForStream(channelName.name).map(q => f("I couldn't find any quotes, man.", q))
          case Some(n) => db.getQuoteByQid(channelName.name, n).map(q => f(s"I couldn't find quote #$n, man.", q))
        }
      }
    )
  }

  val addQuoteCommand: BotCommand =
    BotCommand(
      "!addQuote",
      ModOnly,
      slurp,
      (channelName, chatUser, text: String) => {
        println(s"in addQuoteCommand, $channelName $chatUser $text")
        for {
          oq <- db.insertQuote(text, chatUser.username.name, channelName.name)
        } yield oq match {
          case Some(q) => List(RespondWith(s"Added ${q.display}"))
          case None =>
            List(
              RespondWith(
                s"Something went wrong! I couldn't add stream ${channelName.name}. Somebody tell @artofthetroll"
              )
            )
        }
      }
    )

  val delQuoteCommand: BotCommand =
    BotCommand(
      "!delQuote",
      ModOnly,
      int,
      (channelName, _, n: Int) =>
        for {
          i <- db.deleteQuote(channelName.name, n)
        } yield i match {
          case 1 => List(RespondWith("Ok I deleted it."))
          case _ =>
            List(
              RespondWith(
                s"Something went wrong! I couldn't delete quote $n for channel ${channelName.name}. Somebody tell @artofthetroll"
              )
            )
        }
    )

  val partCommand: BotCommand =
    BotCommand(
      "!part",
      Owner,
      empty,
      (channelName, _, _: Unit) =>
        for {
          _ <- db.partStream(channelName.name)
        } yield List(RespondWith("Goodbye cruel world!"), Part)
    )

  val joinCommand: BotCommand = {
    BotCommand(
      "!join",
      God,
      anyString,
      (_, _, newChannelName: String) =>
        for {
          b <- db.doesStreamExist(newChannelName)
          _ <- if (b) db.insertStream(newChannelName) else db.joinStream(newChannelName)
        } yield List(Join(newChannelName), RespondWith(s"Joining $newChannelName!"))
    )
  }

  val commands: Map[String, BotCommand] = List(
    joinCommand,
    partCommand,
    getQuoteCommand,
    addQuoteCommand,
    delQuoteCommand,
    printStreamsCommand
  ).map(c => (c.name, c)).toMap

  def findAndRun(msg: ChatMessage): IO[List[Response]] = {
    val (name, rest) = msg.body.trim.span(_ != ' ')
    commands.get(name) match {
      case Some(command) =>
        println(s"command: $command")
        runBotCommand(command, msg, rest)
      case None => Nil.pure[IO]
    }
  }

  def runBotCommand(cmd: BotCommand, msg: ChatMessage, args: String): IO[List[Response]] = {
    def go: IO[List[Response]] = {
      cmd.parser.apply(args.trim) match {
        case Success(r, _) => cmd.execute(msg.channel, msg.user, r)
        case Failure(_)    => List(RespondWith("Sorry, I don't understand that.")).pure[IO]
      }
    }

    cmd.permission match {
      case God if isGod(msg.user)                                      => go
      case Owner if isStreamerOrGod(msg.user, msg)                     => go
      case ModOnly if msg.user.isMod || isStreamerOrGod(msg.user, msg) => go
      case Anyone                                                      => go
      case _                                                           => Nil.pure[IO]
    }
  }

  def isStreamerOrGod(chatUser: ChatUser, chatMessage: ChatMessage): Boolean =
    chatUser.username.name.toLowerCase == chatMessage.channel.name.toLowerCase || isGod(chatUser)

  def isGod(chatUser: ChatUser): Boolean = chatUser.username.name.toLowerCase == "artofthetroll"
}