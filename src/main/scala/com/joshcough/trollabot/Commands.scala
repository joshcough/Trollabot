package com.joshcough.trollabot

import ParserCombinators._

trait Permission
  case object Owner extends Permission
  case object ModOnly extends Permission
  case object Anyone extends Permission

trait Response
  case class RespondWith(s: String) extends Response
  case object Join extends Response
  case object Part extends Response

case class ChatUserName(name: String)
case class ChatUser(username: ChatUserName, isMod: Boolean, subscriber: Boolean, badges: Map[String, String])
case class ChannelName(name: String)

trait BotCommand {
  type A
  val name: String
  val permission: Permission
  val parser: Parser[A]
  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): List[Response]
  override def toString: String = s"Command(name: $name, perm: $permission)"
}

object BotCommand {
  def apply[R](commandName: String,
               perm: Permission,
               commandParser: Parser[R],
               f: (ChannelName, ChatUser, R) => List[Response]): BotCommand = {
    new BotCommand {
      override type A = R
      val name: String = commandName
      val permission: Permission = perm
      val parser: Parser[A] = commandParser
      def execute(channelName: ChannelName, chatUser: ChatUser, r: A): List[Response] =
        f(channelName, chatUser, r)
    }
  }
}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case class Commands(trollabotDb: TrollabotDb) {

  val getQuoteCommand: BotCommand = {
    def f(msg: String, mq: Option[Quote]) = List(RespondWith(mq.map(_.display).getOrElse(msg)))
    BotCommand("!quote", Anyone, int.?, (channelName, _, mn: Option[Int]) => {
      mn match {
        case None => f("I couldn't find any quotes, man.", trollabotDb.getRandomQuoteIO(channelName.name))
        case Some(n) => f(s"I couldn't find quote #$n, man.", trollabotDb.getQuoteByQidIO(channelName.name, n))
      }
    })
  }

  val addQuoteCommand: BotCommand =
    BotCommand("!addQuote", ModOnly, slurp, (channelName, chatUser, text: String) => {
      println(s"in addQuoteCommand, $channelName $chatUser $text")
      trollabotDb.insertQuoteIO(text, chatUser.username.name, channelName.name) match {
        case Some(q) => List(RespondWith(s"Added ${q.display}"))
        case None => List(RespondWith(s"Something went wrong! I couldn't add stream ${channelName.name}. Somebody tell @artofthetroll"))
      }
    })

  val delQuoteCommand: BotCommand =
    BotCommand("!delQuote", ModOnly, int, (channelName, _, n: Int) => {
      trollabotDb.deleteQuoteIO(channelName.name, n) match {
        case Some(_) => List(RespondWith("Ok I deleted it."))
        case None => List(RespondWith(s"Something went wrong! I couldn't add stream ${channelName.name}. Somebody tell @artofthetroll"))
      }
    })

  val partCommand: BotCommand =
    BotCommand("!part", Owner, empty, (channelName, _, _: Unit) => {
      trollabotDb.partStream(channelName.name)
      List(RespondWith("Goodbye cruel world!"), Part)
    })

  val joinCommand: BotCommand =
    BotCommand("!join", Owner, empty, (channelName, _, _: Unit) => {
      trollabotDb.joinStream(channelName.name)
      List(RespondWith(s"Joining ${channelName.name}!"), Join)
    })

  val commands: Map[String, BotCommand] = List(
    partCommand,
    getQuoteCommand,
    addQuoteCommand,
    delQuoteCommand,
  ).map(c => (c.name, c)).toMap

  def findAndRun(msg: ChatMessage): List[Response] = {
    val (name, rest) = msg.body.trim.span(_ != ' ')
    commands.get(name) match {
      case Some(command) =>
        println(s"command: $command")
        runBotCommand(command, msg, rest)
      case None => Nil
    }
  }

  def runBotCommand(cmd: BotCommand, msg: ChatMessage, args: String): List[Response] = {
    def go: List[Response] = {
      cmd.parser.apply(args.trim) match {
        case Success(r, _) => cmd.execute(msg.channel, msg.user, r)
        case Failure(_) => List(RespondWith("Sorry, I don't understand that."))
      }
    }

    cmd.permission match {
      case Owner if isStreamerOrGod(msg.user, msg) => go
      case ModOnly if msg.user.isMod || isStreamerOrGod(msg.user, msg) => go
      case Anyone => go
      case _ => Nil
    }
  }

  def isStreamerOrGod(chatUser: ChatUser, chatMessage: ChatMessage): Boolean =
    chatUser.username.name.toLowerCase == chatMessage.channel.name.toLowerCase ||
    chatUser.username.name.toLowerCase == "artofthetroll"
}
