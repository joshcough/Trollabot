package com.joshcough.trollabot.api

import cats.Applicative
import cats.implicits._
import doobie.ConnectionIO

trait HealthCheck[F[_]] {
  def health: F[Unit]
}

object HealthCheck {
  def impl[F[_]: Applicative]: HealthCheck[F] =
    new HealthCheck[F] {
      def health: F[Unit] = ().pure[F]
    }
}

object HealthCheckDb extends HealthCheck[ConnectionIO] {
  def health: ConnectionIO[Unit] = ().pure[ConnectionIO]
}
