package com.joshcough.trollabot.twitch

import com.joshcough.trollabot.{BuildInfo, ChannelName, ChatUser, CounterName, Quote}
import ParserCombinators._
import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.api.Api
import doobie.ConnectionIO
import doobie.implicits.toDoobieStreamOps
import doobie.util.transactor.Transactor
import fs2.{Pure, Stream}
import io.circe.{Codec, Decoder, Encoder}
import io.circe.derivation
import io.circe.generic.auto._
import io.circe.syntax._
import logstage.LogstageCodec
import logstage.circe.LogstageCirceCodec
import logstage.strict.LogIOStrict

sealed trait Permission
case object God extends Permission
case object Owner extends Permission
case object ModOnly extends Permission
case object Anyone extends Permission

sealed trait Response

object Response {
  implicit val encodeResponse: Encoder[Response] = Encoder.instance {
    case r @ RespondWith(_) => r.asJson
    case r @ Join(_)        => r.asJson
    case _ @Part            => "Part".asJson // TODO: is this correct, or BS?
    case r @ LogErr(_)      => r.asJson
  }
  implicit val decodeResponse: Decoder[Response] =
    List[Decoder[Response]](
      Decoder[RespondWith].widen,
      Decoder[Join].widen,
      Decoder[Part.type].widen,
      Decoder[LogErr].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Response] = LogstageCirceCodec.derived[Response]
}

case class RespondWith(s: String) extends Response
case class Join(newChannel: ChannelName) extends Response
case object Part extends Response {
  implicit val circeCodec: Codec[Part.type] = derivation.deriveCodec[Part.type]
  implicit val logstageCodec: LogstageCodec[Part.type] = LogstageCirceCodec.derived[Part.type]
}
case class LogErr(err: String) extends Response

object LogErr {
  def apply(t: Throwable): LogErr = LogErr(t.getMessage + "\n" + t.getStackTrace.mkString(","))
}

object RespondWith {
  implicit val circeCodec: Codec[RespondWith] = derivation.deriveCodec[RespondWith]
  implicit val logstageCodec: LogstageCodec[RespondWith] = LogstageCirceCodec.derived[RespondWith]
}

object Join {
  implicit val circeCodec: Codec[Join] = derivation.deriveCodec[Join]
  implicit val logstageCodec: LogstageCodec[Join] = LogstageCirceCodec.derived[Join]
}

sealed trait Action
case object PrintStreamsAction extends Action
case class GetExactQuoteAction(channelName: ChannelName, qid: Int) extends Action
case class SearchQuotesAction(channelName: ChannelName, like: String) extends Action
case class GetRandomQuoteAction(channelName: ChannelName) extends Action
case class AddQuoteAction(channelName: ChannelName, chatUser: ChatUser, text: String) extends Action
case class DelQuoteAction(channelName: ChannelName, n: Int) extends Action
// TODO: we should keep track of the user who parted.
case class PartAction(channelName: ChannelName) extends Action
case class JoinAction(newChannelName: ChannelName) extends Action
// TODO: eventually we want this: // !commandName ${c} words words ${c++} words words ${++c} words.
case class AddCounterAction(channelName: ChannelName, chatUser: ChatUser, counterName: CounterName)
    extends Action
case class IncCounterAction(channelName: ChannelName, counterName: CounterName) extends Action
case class HelpAction(commandName: String) extends Action
case object BuildInfoAction extends Action
case class ScoreAction(channelName: ChannelName, scoreArg: ScoreArg) extends Action
case class SetPlayerAction(channelName: ChannelName, playerName: String) extends Action
case class SetOpponentAction(channelName: ChannelName, playerName: String) extends Action

sealed trait ScoreArg
case object GetScore extends ScoreArg
case class SetScore(player1Score: Int, player2Score: Int) extends ScoreArg
case class SetAll(player1: String, player2: String, player1Score: Int, player2Score: Int)
    extends ScoreArg

object ScoreArg {
  implicit val encodeResponse: Encoder[ScoreArg] = Encoder.instance {
    case _ @GetScore            => "GetScore".asJson
    case r @ SetScore(_, _)     => r.asJson
    case r @ SetAll(_, _, _, _) => r.asJson
  }

  implicit val decodeResponse: Decoder[ScoreArg] =
    List[Decoder[ScoreArg]](
      Decoder[GetScore.type].widen,
      Decoder[SetScore].widen,
      Decoder[SetAll].widen
    ).reduceLeft(_ or _)
}

object Action {
  implicit val encodeResponse: Encoder[Action] = Encoder.instance {
    case _ @PrintStreamsAction         => "PrintStreamsAction".asJson
    case r @ GetExactQuoteAction(_, _) => r.asJson
    case r @ SearchQuotesAction(_, _)  => r.asJson
    case r @ GetRandomQuoteAction(_)   => r.asJson
    case r @ AddQuoteAction(_, _, _)   => r.asJson
    case r @ DelQuoteAction(_, _)      => r.asJson
    case r @ PartAction(_)             => r.asJson
    case r @ JoinAction(_)             => r.asJson
    case r @ AddCounterAction(_, _, _) => r.asJson
    case r @ IncCounterAction(_, _)    => r.asJson
    case r @ HelpAction(_)             => r.asJson
    case _ @BuildInfoAction            => "BuildInfoAction".asJson
    case r @ ScoreAction(_, _)         => r.asJson
    case r @ SetPlayerAction(_, _)     => r.asJson
    case r @ SetOpponentAction(_, _)   => r.asJson
  }
  implicit val decodeResponse: Decoder[Action] =
    List[Decoder[Action]](
      Decoder[PrintStreamsAction.type].widen,
      Decoder[GetExactQuoteAction].widen,
      Decoder[SearchQuotesAction].widen,
      Decoder[GetRandomQuoteAction].widen,
      Decoder[AddQuoteAction].widen,
      Decoder[DelQuoteAction].widen,
      Decoder[PartAction].widen,
      Decoder[JoinAction].widen,
      Decoder[AddCounterAction].widen,
      Decoder[IncCounterAction].widen,
      Decoder[HelpAction].widen,
      Decoder[BuildInfoAction.type].widen,
      Decoder[ScoreAction].widen,
      Decoder[SetPlayerAction].widen,
      Decoder[SetOpponentAction].widen
    ).reduceLeft(_ or _)
  implicit val logstageCodec: LogstageCodec[Action] = LogstageCirceCodec.derived[Action]
}

trait BotCommand {
  type A
  type ActionType <: Action
  val name: String
  val parser: Parser[A]

  val permission: ActionType => Permission

  override def toString: String = s"$name ${parser.describe}"
  def help: String = toString // TODO: maybe we change this around later, but its fine for now.

  def execute(channelName: ChannelName, chatUser: ChatUser, a: A): ActionType

  def apply(
      channelName: ChannelName,
      chatUser: ChatUser,
      args: String
  ): Either[String, ActionType] =
    parser(args.trim).toEither.map(a => execute(channelName, chatUser, a))

  // returns true if the given user is allowed to run the command on the given channel
  // false if not.
  def hasPermssion(channelName: ChannelName, chatUser: ChatUser, a: ActionType): Boolean = {
    def isStreamer = chatUser.username.name.toLowerCase == channelName.name.toLowerCase
    def isGod: Boolean = chatUser.username.name.toLowerCase == "artofthetroll"
    permission(a) match {
      case God     => isGod
      case Owner   => isStreamer || isGod
      case ModOnly => chatUser.isMod || isStreamer || isGod
      case Anyone  => true
    }
  }
}

object BotCommand {
  def apply[T, AT <: Action](cmdName: String, cmdParser: Parser[T], perm: AT => Permission)(
      f: (ChannelName, ChatUser, T) => AT
  ): BotCommand =
    new BotCommand {
      override type A = T
      override type ActionType = AT
      val name: String = cmdName
      val permission: ActionType => Permission = perm
      val parser: Parser[T] = cmdParser
      def execute(channelName: ChannelName, chatUser: ChatUser, t: T): AT =
        f(channelName, chatUser, t)
    }
}

case class CommandInterpreter(api: Api[ConnectionIO]) {
  import api._

  def interpret(a: Action): Stream[ConnectionIO, Response] =
    a match {
      case PrintStreamsAction                          => printStreams
      case GetExactQuoteAction(channelName, qid)       => getExactQuote(channelName, qid)
      case GetRandomQuoteAction(channelName)           => getRandomQuote(channelName)
      case SearchQuotesAction(channelName, like)       => search(channelName, like)
      case AddQuoteAction(channelName, chatUser, text) => addQuote(channelName, chatUser, text)
      case DelQuoteAction(channelName, qid)            => deleteQuote(channelName, qid)
      case PartAction(channelName)                     => part(channelName)
      case JoinAction(newChannelName)                  => join(newChannelName)
      case AddCounterAction(channel, user, counter)    => addCounter(channel, user, counter)
      case IncCounterAction(channel, counter)          => incCounter(channel, counter)
      case HelpAction(commandName)                     => help(commandName, Commands.commands)
      case BuildInfoAction                             => buildInfo
      case ScoreAction(channel, GetScore)              => getScore(channel)
      case ScoreAction(channel, SetScore(p1s, p2s))    => setScore(channel, p1s, p2s)
      case ScoreAction(channel, SetAll(p1, p2, p1s, p2s)) =>
        setScoreAndPlayers(channel, p1, p2, p1s, p2s)
      case SetPlayerAction(channelName, p1)   => setPlayer(channelName, p1)
      case SetOpponentAction(channelName, p1) => setOpponent(channelName, p1)
    }

  def printStreams: Stream[ConnectionIO, Response] =
    streams.getStreams.map(_.asJson.noSpaces).reduce((l, r) => s"$l,$r").map(RespondWith(_))

  def getExactQuote(channelName: ChannelName, qid: Int): Stream[ConnectionIO, Response] =
    withQuoteOr(quotes.getQuote(channelName, qid), s"I couldn't find quote #$qid, man.")

  def getRandomQuote(channelName: ChannelName): Stream[ConnectionIO, Response] =
    withQuoteOr(quotes.getRandomQuote(channelName), "I couldn't find any quotes, man.")

  // TODO: this take(1) here is a little sus.
  // What do we really want to return here? Maybe we just want to return a link...
  // or a random one? Who knows.
  def search(channelName: ChannelName, like: String): Stream[ConnectionIO, Response] =
    Stream.eval(quotes.searchQuotes_Random(channelName, like).map {
      case Some(q) => RespondWith(q.display)
      case None    => RespondWith("Couldn't find any quotes that match that.")
    })

  def addQuote(
      channelName: ChannelName,
      chatUser: ChatUser,
      text: String
  ): Stream[ConnectionIO, Response] = {
    val q = quotes.insertQuote(text, chatUser.username, channelName).map {
      case Right(q) => RespondWith(q.display)
      case Left(q)  => RespondWith(s"That quote already exists man! It's #${q.qid}")
    }
    def onErr(e: Throwable): Stream[Pure, Response] =
      errHandler(e, s"I couldn't add quote for stream ${channelName.name}")
    Stream.eval(q).handleErrorWith(onErr)
  }

  // TODO: maybe we should mark the quote deleted instead of deleting it
  // and then we could take the user who deleted it too.
  // we could add two new columns to quote: deletedAt and deletedBy
  def deleteQuote(channelName: ChannelName, n: Int): Stream[ConnectionIO, Response] =
    Stream.eval(quotes.deleteQuote(channelName, n).map {
      case true  => RespondWith("Ok I deleted it.")
      case false => RespondWith(err(s"I couldn't delete quote $n for channel ${channelName.name}"))
    })

  def part(channelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream
      .eval(streams.markParted(channelName))
      .flatMap(_ => Stream(RespondWith("Goodbye cruel world!"), Part))

  def join(newChannelName: ChannelName): Stream[ConnectionIO, Response] =
    Stream
      .eval(streams.join(newChannelName))
      .flatMap(_ => Stream(Join(newChannelName), RespondWith(s"Joining ${newChannelName.name}!")))

  def addCounter(
      channelName: ChannelName,
      chatUser: ChatUser,
      counterName: CounterName
  ): Stream[ConnectionIO, Response] =
    Stream
      .eval(
        counters
          .insertCounter(channelName, chatUser, counterName)
          .map(c => RespondWith(s"Ok I added it. ${c.name.name}:${c.count}"))
      )
      .handleErrorWith { e =>
        errHandler(e, s"I couldn't add counter for ${counterName.name} stream ${channelName.name}")
      }

  def incCounter(
      channelName: ChannelName,
      counterName: CounterName
  ): Stream[ConnectionIO, Response] =
    Stream
      .eval(
        counters
          .incrementCounter(channelName, counterName)
          .map(c => RespondWith(s"Ok I incremented it. ${c.name.name}:${c.count}"))
      )
      .handleErrorWith { e =>
        errHandler(
          e,
          s"I couldn't increment counter for ${counterName.name} stream ${channelName.name}"
        )
      }

  def help(
      commandName: String,
      knownCommands: Map[String, BotCommand]
  ): Stream[ConnectionIO, Response] =
    Stream.emit(RespondWith(knownCommands.get(commandName) match {
      case None    => s"Unknown command: $commandName"
      case Some(c) => c.toString
    }))

  val buildInfo: Stream[ConnectionIO, Response] =
    Stream.emit(RespondWith(BuildInfo().asJson.noSpaces))

  def getScore(channel: ChannelName): Stream[ConnectionIO, Response] =
    Stream.eval(api.scores.getScore(channel).map(s => RespondWith(s.display)))

  def setScore(channel: ChannelName, p1s: Int, p2s: Int): Stream[ConnectionIO, Response] =
    Stream.eval(api.scores.setScore(channel, p1s, p2s).map(s => RespondWith(s.display)))

  def setPlayer(channel: ChannelName, p1: String): Stream[ConnectionIO, Response] =
    Stream.eval(api.scores.setPlayer1(channel, p1).map(s => RespondWith(s.display)))

  def setOpponent(channel: ChannelName, p2: String): Stream[ConnectionIO, Response] =
    Stream.eval(api.scores.setPlayer2(channel, p2).map(s => RespondWith(s.display)))

  def setScoreAndPlayers(
      channel: ChannelName,
      p1: String,
      p2: String,
      p1s: Int,
      p2s: Int
  ): Stream[ConnectionIO, Response] =
    Stream.eval(api.scores.setAll(channel, p1, p1s, p2, p2s).map(s => RespondWith(s.display)))

  private def withQuoteOr(
      foq: ConnectionIO[Option[Quote]],
      msg: String
  ): Stream[ConnectionIO, Response] =
    Stream.eval(foq.map(oq => RespondWith(oq.map(_.display).getOrElse(msg))))

  private def err(msg: String): String = s"Something went wrong! $msg. Somebody tell @artofthetroll"

  private def errHandler(e: Throwable, msg: String): Stream[Pure, Response] =
    Stream.emits(List[Response](RespondWith(err(msg)), LogErr(e)))
}

object CommandInterpreter {
  // This class is just for logging.
  case class Executing(cmdName: String, user: ChatUser, channel: ChannelName, action: Action)
  object Executing {
    implicit val circeCodec: Codec[Executing] = derivation.deriveCodec[Executing]
    implicit val logstageCodec: LogstageCodec[Executing] = LogstageCirceCodec.derived[Executing]
  }

  def interpret[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      cmd: BotCommand,
      args: String,
      api: Api[ConnectionIO],
      xa: Transactor[F],
      L: LogIOStrict[F]
  ): Either[String, Stream[F, Response]] =
    cmd.apply(msg.channel, msg.user, args).map { action =>
      val e = Executing(cmd.name, msg.user, msg.channel, action)

      if (cmd.hasPermssion(msg.channel, msg.user, action)) for {
        _ <- Stream.eval(L.debug(s"executing $e"))
        res <- CommandInterpreter(api).interpret(action).transact(xa)
        _ <- Stream.eval(L.debug(s"done executing $e"))
      } yield res
      // user doesnt' have permission to execute this command, so just do nothing.
      // we _could_ send back a message saying "you can't do that", but i think its too noisy.
      else
        Stream
          .eval(L.debug(s"user ${msg.user} lacks permission to run: ${cmd.name}")) *> Stream.empty
    }

}

case class ChatMessage(user: ChatUser, channel: ChannelName, body: String)

case object Commands {

  val channelNameParser: Parser[ChannelName] = anyStringAs("channel name").map(ChannelName(_))
  val counterNameParser: Parser[CounterName] = anyStringAs("counter name").map(CounterName(_))
  val commandNameParser: Parser[String] = anyStringAs("command_name")

  def const[A, B](b: B): A => B = _ => b

  val printStreamsCommand: BotCommand =
    BotCommand[Unit, PrintStreamsAction.type]("!printStreams", empty, const(God))((_, _, _) =>
      PrintStreamsAction
    )

  val getQuoteCommand: BotCommand =
    BotCommand[Option[Int], Action]("!quote", int.?, const(Anyone))((channelName, _, mn) =>
      mn.fold[Action](GetRandomQuoteAction(channelName))(n => GetExactQuoteAction(channelName, n))
    )

  val searchQuotesCommand: BotCommand =
    BotCommand[String, SearchQuotesAction]("!search", slurp, const(Anyone))(
      (channelName, _, like) => SearchQuotesAction(channelName, like)
    )

  val addQuoteCommand: BotCommand =
    BotCommand[String, AddQuoteAction]("!addQuote", slurp, const(ModOnly))(
      (channelName, chatUser, text) => AddQuoteAction(channelName, chatUser, text)
    )

  val delQuoteCommand: BotCommand =
    BotCommand[Int, DelQuoteAction]("!delQuote", int, const(ModOnly))((channelName, _, n) =>
      DelQuoteAction(channelName, n)
    )

  val partCommand: BotCommand =
    BotCommand[Unit, PartAction]("!part", empty, const(Owner))((c, _, _) => PartAction(c))

  val joinCommand: BotCommand =
    BotCommand[ChannelName, JoinAction]("!join", channelNameParser, const(God))(
      (_, _, newChannelName) => JoinAction(newChannelName)
    )

  val addCounterCommand: BotCommand =
    BotCommand[CounterName, AddCounterAction]("!addCounter", counterNameParser, const(God))(
      (channelName, chatUser, name) => AddCounterAction(channelName, chatUser, name)
    )

  val incCounterCommand: BotCommand =
    BotCommand[CounterName, IncCounterAction]("!inc", counterNameParser, const(Anyone))(
      (channelName, _, name) => IncCounterAction(channelName, name)
    )

  val helpCommand: BotCommand =
    BotCommand[String, HelpAction]("!help", commandNameParser, const(Anyone))((_, _, command) =>
      HelpAction(command)
    )

  val buildInfoCommand: BotCommand =
    BotCommand[Unit, BuildInfoAction.type]("!buildInfo", empty, const(God))((_, _, _) =>
      BuildInfoAction
    )

  val scoreCommand: BotCommand = {
    val scoreParser: Parser[ScoreArg] =
      eof.named("no arguments").^^^(GetScore) |
        (int.named("p1_score") ~ int.named("p2_score")).map { case p1 ~ p2 => SetScore(p1, p2) } |
        (anyString.named("p1_name") ~ int.named("p1_score") ~
          anyString.named("p2_name") ~ int.named("p2_score")).map {
          case p1 ~ p1Score ~ p2 ~ p2Score => SetAll(p1, p2, p1Score, p2Score)
        }
    // this is dumb, it should really be a ScoreAction.
    def perms(a: ScoreAction): Permission =
      a.scoreArg match {
        case GetScore => Anyone
        case _        => ModOnly
      }
    BotCommand[ScoreArg, ScoreAction]("!score", scoreParser, perms)((channelName, _, scoreArg) =>
      ScoreAction(channelName, scoreArg)
    )
  }

  val playerCommand: BotCommand =
    BotCommand[String, SetPlayerAction]("!player", anyStringAs("player_name"), const(ModOnly))(
      (channelName, _, name) => SetPlayerAction(channelName, name)
    )

  val opponentCommand: BotCommand =
    BotCommand[String, SetOpponentAction]("!opponent", anyStringAs("player_name"), const(ModOnly))(
      (channelName, _, name) => SetOpponentAction(channelName, name)
    )

  val commands: Map[String, BotCommand] = List(
    joinCommand,
    partCommand,
    getQuoteCommand,
    searchQuotesCommand,
    addQuoteCommand,
    delQuoteCommand,
    printStreamsCommand,
    addCounterCommand,
    incCounterCommand,
    helpCommand,
    buildInfoCommand,
    scoreCommand,
    playerCommand,
    opponentCommand
  ).map(c => (c.name, c)).toMap

}

case class CommandRunner(commands: Map[String, BotCommand]) {
  // returns the command (if it exists) and the arguments to be passed to the command
  def parseMessageAndFindCommand(msg: ChatMessage): Option[(BotCommand, String)] = {
    val (commandName, args) = msg.body.trim.span(_ != ' ')
    commands.get(commandName).map((_, args))
  }

  def processMessage[F[_]: MonadCancelThrow](
      msg: ChatMessage,
      api: Api[ConnectionIO],
      xa: Transactor[F]
  )(implicit
      L: LogIOStrict[F]
  ): Stream[F, Response] =
    parseMessageAndFindCommand(msg) match {
      case Some((cmd, args)) =>
        CommandInterpreter.interpret(msg, cmd, args, api, xa, L) match {
          case Left(_)  => Stream(RespondWith(cmd.help))
          case Right(r) => r
        }
      // no command for this chat message, so just do nothing.
      case None => Stream.empty
    }
}
