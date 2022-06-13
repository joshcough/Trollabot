package com.joshcough.trollabot.web

import cats.effect.MonadCancelThrow
import cats.implicits._
import com.joshcough.trollabot.{Stream, TrollabotDb}
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import io.circe.derivation.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Count(i: Int)
object Count {
  implicit val countEncoder: Encoder[Count] = deriveEncoder[Count]
  implicit def countEntityEncoder[F[_]]: EntityEncoder[F, Count] = jsonEncoderOf
}

trait Inspections[F[_]] {
  def getAllStreams: fs2.Stream[F, Stream]
  def getJoinedStreams: fs2.Stream[F, Stream]
  def countQuotes: F[Count]
  def countQuotesInStream(streamName: String): F[Count]
}

object Inspections {
  def apply[F[_]](implicit ev: Inspections[F]): Inspections[F] = ev

  def impl[F[_]: MonadCancelThrow](xa: Transactor[F]): Inspections[F] =
    new Inspections[F] {
      val db = TrollabotDb
      override def getAllStreams: fs2.Stream[F, Stream] = db.getAllStreams.transact(xa)
      override def getJoinedStreams: fs2.Stream[F, Stream] = db.getJoinedStreams.transact(xa)
      override def countQuotes: F[Count] = db.countQuotes.transact(xa).map(Count(_))
      override def countQuotesInStream(streamName: String): F[Count] =
        db.countQuotesInStream(streamName).transact(xa).map(Count(_))
    }
}
