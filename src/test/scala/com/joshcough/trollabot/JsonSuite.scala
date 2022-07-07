package com.joshcough.trollabot

import api._
import com.joshcough.trollabot.TimestampInstances._
import com.joshcough.trollabot.twitch.commands.{Action, ChatMessage, Response}
import com.joshcough.trollabot.twitch.commands.Scores.ScoreArg
import com.mrdziuban.ScalacheckMagnolia._
import io.circe.{Decoder, Encoder, jawn}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import java.sql.Timestamp
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Prop}

import ArbitraryInstances._

class JsonSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  val channel: ChannelName = ChannelName("daut")
  val user: ChatUserName = ChatUserName("artofthetroll")
  val ts: Timestamp = Timestamp.valueOf("2022-07-04 06:42:00")

  def go[A: Encoder](a: A, expected: String) = assertEquals(a.asJson.noSpaces, expected)

  test("Can turn all the api models to json") {
    go(
      Stream(channel, joined = true, user, ts),
      """{"name":{"name":"daut"},"joined":true,"addedBy":{"name":"artofthetroll"},"addedAt":1656934920000}"""
    )
    go(
      Quote(None, 1, "hi", channel, user, ts, deleted = false, None, None),
      """{"id":null,"qid":1,"text":"hi","channel":{"name":"daut"},"addedBy":{"name":"artofthetroll"},"addedAt":1656934920000,"deleted":false,"deletedBy":null,"deletedAt":null}"""
    )
    go(
      Counter(None, CounterName("housed"), 42, channel, user, ts),
      """{"id":null,"name":{"name":"housed"},"count":42,"channel":{"name":"daut"},"addedBy":{"name":"artofthetroll"},"addedAt":1656934920000}"""
    )
    go(
      Score(None, channel, Some("daut"), Some("viper"), 4, 0),
      """{"id":null,"channel":{"name":"daut"},"player1":"daut","player2":"viper","player1Score":4,"player2Score":0}"""
    )
    go(
      UserCommand(
        None,
        UserCommandName("housed"),
        "Been housed ${housed} times!",
        channel,
        user,
        ts
      ),
      """{"id":null,"name":{"namePotentiallyWithBang":"housed"},"body":"Been housed ${housed} times!","channel":{"name":"daut"},"addedBy":{"name":"artofthetroll"},"addedAt":1656934920000}"""
    )
  }

  test("Can round trip all the models to json and back again") {
    roundTripForAll[UserCommandName]
    roundTripForAll[UserCommand]
    roundTripForAll[ChatMessage]
    roundTripForAll[Response]

    roundTripForAll[Quote]
    roundTripForAll[Stream]
    roundTripForAll[Counter]
    roundTripForAll[Score]
    roundTripForAll[ScoreArg]

    roundTripForAll[Action]
  }

  def roundTrip[A: Encoder: Decoder](a: A): Either[io.circe.Error, A] = {
    //println(a.asJson.noSpaces)
    jawn.decode[A](a.asJson.noSpaces)
  }

  def roundTripForAll[A: Encoder: Decoder: Arbitrary]: Prop =
    forAll { (a: A) => roundTrip(a) == Right(a) }
}
