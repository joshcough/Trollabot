package com.joshcough.trollabot.db

import cats.effect.IO
import cats.implicits._
import com.joshcough.trollabot.Configuration
import logstage.strict.LogIOStrict
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.Location
import org.flywaydb.core.Flyway
import scala.jdk.CollectionConverters._

object Migrations {

  def migrate(config: Configuration)(implicit L: LogIOStrict[IO]): IO[Int] = {
    val m: FluentConfiguration = Flyway.configure
      .dataSource(config.db.jdbcUrl, config.db.user, config.db.password)
      .group(true)
      .outOfOrder(false)
      .table("migrations")
      .locations(List(new Location("classpath:migrations")): _*)
      .baselineOnMigrate(true)

    for {
      _ <- logValidationErrorsIfAny(m)
      i <- IO(m.load().migrate().migrationsExecuted)
    } yield i
  }

  private def logValidationErrorsIfAny(
      m: FluentConfiguration
  )(implicit L: LogIOStrict[IO]): IO[Unit] =
    for {
      v <- IO(m.ignorePendingMigrations(true).load().validateWithResult())
      _ <- v.validationSuccessful match {
        case true => ().pure[IO]
        case false =>
          v.invalidMigrations.asScala.toList.traverse_ { error =>
            val msg = s"""
                       |Failed validation:
                       |  - version: ${error.version}
                       |  - path: ${error.filepath}
                       |  - description: ${error.description}
                       |  - errorCode: ${error.errorDetails.errorCode}
                       |  - errorMessage: ${error.errorDetails.errorMessage}
                       """.stripMargin.trim
            L.debug(s"$msg")
          }
      }
    } yield ()
}
