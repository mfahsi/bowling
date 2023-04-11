package com.descartes.bowling.persistence

import cats.effect.IO
import cats.effect.kernel.Resource
import com.descartes.bowling.config.{AppConfig, DatabaseConfig, DbType}
import com.descartes.bowling.domain.{BowlingGame, DomainError, GameNotFoundError, Roll}
import doobie.hikari.HikariTransactor
import doobie.implicits.toSqlInterpolator
import doobie.util.fragment
import doobie.util.transactor.Transactor
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

trait GameRepository {
  import scala.concurrent.ExecutionContext
  import doobie.implicits._
  import doobie.util.transactor.Transactor

  val logger = LoggerFactory.getLogger(getClass)

  val dbInitScript : Option[fragment.Fragment] = None

  def transactor() : Transactor[IO]

  def insert(game: BowlingGame): IO[Long]

  def updateGame(id: Long, g: BowlingGame): IO[Either[GameNotFoundError, BowlingGame]]

  def findById(id: Long): IO[Option[BowlingGame]]

  def deleteGame(id: Long): IO[Either[GameNotFoundError, Unit]]

  def updateGameOpt(id: Long, g: Option[Either[DomainError,BowlingGame]]): IO[Either[DomainError, BowlingGame]] = {
    g.map(gEither => gEither match {
      case Left(e) => IO.pure(Left(e))
      case Right(game) => updateGame(id, game)
    }).getOrElse({
      logger.info("roll or update : game {} not found", id)
      IO.pure(Left(GameNotFoundError(id)))
    })
  }

  def initDatabase(): Transactor[IO]=> IO[Int] = (transactor: Transactor[IO]) => {
    if(dbInitScript.isDefined){
      logger.info("Running Init Script for H2 database")
      dbInitScript.get.update.run.transact(transactor)
    }else{
      logger.info("No sql startup script")
      IO.pure(0)
    }
  }
}

object GameRepository {
  val logger = LoggerFactory.getLogger(getClass)

  def transactor(config: DatabaseConfig, executionContext: ExecutionContext): Resource[IO, Transactor[IO]] = {
    val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "",
      ""
    )
    logger.info("database configuration {}",config)
    if(config.driver.contains("org.h2")){
      logger.info("IN Memory database configuration {}",config)
      Resource.pure(Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password
      ))
    }else {
      logger.info("Postgres database configuration {}",config)
      HikariTransactor.newHikariTransactor[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
        executionContext
      )
    }
  }
}
