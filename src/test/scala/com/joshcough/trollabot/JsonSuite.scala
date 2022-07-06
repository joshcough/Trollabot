package com.joshcough.trollabot

import api._
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}

import java.sql.Timestamp

import io.circe.generic.auto._
import com.joshcough.trollabot.TimestampInstances._


class JsonSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  val channel = ChannelName("daut")
  val user = ChatUserName("artofthetroll")
  val ts = Timestamp.valueOf("2022-07-04 06:42:00")

  def go[A: Encoder](a: A, expected: String) = assertEquals(a.asJson.noSpaces, expected)

  test("Can turn all the api models to json") {
    go(Stream(channel, joined = true), """{"name":{"name":"daut"},"joined":true}""")
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

  test("Can turn all the twitch chat models to json") {}
}
