package com.descartes.bowling.config

import cats.data.Reader
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.implicits.catsSyntaxSemigroupal
import com.descartes.bowling.persistence.{GameRepository, PersistenceFactory}
import com.typesafe.config.ConfigFactory
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import pureconfig._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

import scala.reflect.ClassTag

object AppConfig {

  import pureconfig.generic.auto._

  val app_config = "application.conf"
  val makeDbType: Resource[IO, DbType] = load[DbTypeConfigW](app_config).map(_.storage).map(_.db)
  val makeDb: Reader[DbType, Resource[IO, DatabaseConfig]] = Reader(fn => load[GameDatabase](s"gamedb-${fn.toString}.conf").map(_.database))
  val makeApi: Resource[IO, ServerConfig] = load[BowlingApi](app_config).map(_.server)

  val makeApp = for {
    db <- makeDb
  } yield (makeDbType.product(db)).product(makeApi)
    .map { case ((dbType, dbConfig), api) =>
      BowlingAppConfig(api, dbConfig, dbType)
    }


  private def load[T <: Product](configFile: String = app_config)(implicit configReader: ConfigReader[T], tag: ClassTag[T]): Resource[IO, T] = {
    Resource.eval(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, T]())
  }

  def appWithDbConfig(dbType: DbType): Resource[IO, BowlingAppConfig] = makeApp.run(dbType)

  val makeAppRepo = for {
    dbType <- makeDbType
  } yield makeCustomRepository.run(dbType)

  val makeTransactor: Reader[DbType, Resource[IO, Resource[IO, Transactor[IO]]]] = Reader(dbType =>
    for {
      dbConfig <- AppConfig.makeDb.run(dbType)
      ec <- ExecutionContexts.fixedThreadPool[IO](dbConfig.threadPoolSize)
    } yield GameRepository.transactor(dbConfig, ec))

  val makeCustomRepository: Reader[DbType, Resource[IO, GameRepository]] = Reader(dbType =>
    for {
      transactorR <- makeTransactor.run(dbType)
      transactor <- transactorR
    } yield PersistenceFactory.makeGameRepository(dbType, transactor))

}
