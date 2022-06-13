package com.joshcough.trollabot

import cats.effect.{IO, Sync}
import izumi.functional.mono.SyncSafe
import logstage.strict.{IzStrictLogger, LogIOStrict}
import logstage.{ConsoleSink, Trace}

object Logging {

  /*
   * We are using the `IzStrictLogger` to prevent automatic encoding of case classes.
   *  Automatic encoding would result in something like `case class Test(a: Int)` being encoded in the logs as
   *  "Test(2)". Instead, we will (strictly) provide the `LogstageCodec`, resulting in a proper encoding of
   *  "{test: 2}".
   */
  def impl[F[_]](izLogger: IzStrictLogger)(implicit S: Sync[F]): LogIOStrict[F] = {
    /*
     * LogStage conventionally depends on Cats Effect 2.x, so we're creating the
     * SyncSafe instance from the Cats Effect 3 type class.
     */
    val syncSafeInstance: SyncSafe[F] = new SyncSafe[F] {
      override def syncSafe[A](unexceptionalEff: => A): F[A] =
        S.delay(unexceptionalEff)
    }
    LogIOStrict.fromLogger[F](izLogger)(syncSafeInstance)
  }

}

object LoggingImplicits {
  implicit val productionLogger: LogIOStrict[IO] = Logging.impl[IO](IzStrictLogger(Trace, List(ConsoleSink.text(colored = false))))
}
