package com.joshcough.trollabot

import slick.jdbc.PostgresProfile.api._

case class Chatbot(db: Database) {

  val trollabotDb: TrollabotDb = TrollabotDb(db)
  val commands: Commands = Commands(trollabotDb)

  val irc: Irc = Irc(chatMessage => {
    val responses = commands.findAndRun(chatMessage)
    responses.foreach {
      case RespondWith(s) => irc.privMsg(chatMessage.channel.name, s)
      case Join(newChannel) => join(newChannel)
      case Part => irc.part(chatMessage.channel.name)
    }
  })

  def join(streamName:String): Unit = {
    irc.join(streamName)
    irc.privMsg(streamName, s"Hola mi hombres muy estupido!")
  }

  def run(): Unit = {
    irc.login()
    val streams = trollabotDb.getJoinedStreamsIO()
    println("Joining these streams: " + streams)
    streams.foreach(s => join(s.name))
    irc.processMessages()
    println("Done processing messages, shutting down.")
  }

  def close(): Unit = {
    irc.close()
    println("Trollabot shutting down!")
  }
}
