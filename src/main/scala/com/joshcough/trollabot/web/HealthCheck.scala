package com.joshcough.trollabot.web

import cats.Applicative
import cats.implicits._

trait HealthCheck[F[_]] {
  def health: F[Unit]
}

object HealthCheck {
  implicit def apply[F[_]](implicit ev: HealthCheck[F]): HealthCheck[F] = ev

  def impl[F[_]: Applicative]: HealthCheck[F] =
    new HealthCheck[F] {
      def health: F[Unit] = ().pure[F]
    }
}
