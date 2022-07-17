package com.joshcough.trollabot.db.queries

import com.joshcough.trollabot.ChannelName
import com.joshcough.trollabot.api.Score
import doobie._
import doobie.implicits._

object Scores {

  def insertChannel(channelName: ChannelName): Update0 =
    sql"""insert into scores (channel, player1_score, player2_score)
         values (${channelName.name}, 0, 0)""".update

  def setPlayer1(channelName: ChannelName, player1: String): Query0[Score] =
    updateScores(channelName, fr"set player1 = $player1")

  def setPlayer2(channelName: ChannelName, player2: String): Query0[Score] =
    updateScores(channelName, fr"set player2 = $player2")

  def setPlayer1Score(channelName: ChannelName, player1Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player1_score = $player1Score")

  def setPlayer2Score(channelName: ChannelName, player2Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player2_score = $player2Score")

  def setScore(channelName: ChannelName, player1Score: Int, player2Score: Int): Query0[Score] =
    updateScores(channelName, fr"set player1_score = $player1Score, player2_score = $player2Score")

  def setAll(
      channelName: ChannelName,
      player1: String,
      player1Score: Int,
      player2: String,
      player2Score: Int
  ): Query0[Score] =
    updateScores(channelName, fr"""set player1 = $player1, player2 = $player2,
               player1_score = $player1Score, player2_score = $player2Score""")

  def getScore(channelName: ChannelName): Query0[Score] =
    sql"""select channel, player1, player2, player1_score, player2_score
          from scores where channel = ${channelName.name}""".query[Score]

  def updateScores(channelName: ChannelName, fragment: Fragment): Query0[Score] =
    (fr"update scores" ++ fragment ++
      fr"""where channel = ${channelName.name}
           returning channel, player1, player2, player1_score, player2_score""")
      .query[Score]
}
