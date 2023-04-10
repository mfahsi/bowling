package com.descartes.bowling.persistence

import cats.effect.IO
import cats.effect.kernel.Resource
import com.descartes.bowling.config.DatabaseConfig
import com.descartes.bowling.domain.{BowlingGame, Frame, GameNotFoundError, Roll}
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import doobie.syntax.SqlInterpolator.SingleFragment.fromWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext


class GameRepositoryPostgres(transactor: Transactor[IO]) extends GameRepository {

  implicit val rollListMeta: Meta[List[Roll]] =
    Meta[String].timap(parseJson)(_.asJson.noSpaces)

  implicit val frameListMeta: Meta[List[Frame]] =
    Meta[String].timap(parseJsonFrame)(_.asJson.noSpaces)

  implicit val decoder: Decoder[Frame] = deriveDecoder[Frame]
  implicit val encoder: Encoder[Frame] = deriveEncoder[Frame]


  def transactor() = transactor

  def insert(game: BowlingGame): IO[Long] = {
    sql"""
        INSERT INTO Game (player, lane, frames)
        VALUES (${game.player},${game.lane},${game.frames}::json)
      """.update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
  }

  def updateGame(id: Long, g: BowlingGame): IO[Either[GameNotFoundError, BowlingGame]] = {
    sql"UPDATE Game SET frames = ${g.frames}::json WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        logger.info("updateGame updated game {} value {}",id,g)
        Right(g.copy(id = Some(id)))
      } else {
        logger.warn("updateGame game not found {}",id)
        Left(GameNotFoundError(id))
      }
    }
  }

  def findById(id: Long): IO[Option[BowlingGame]] = {
    sql"""
        SELECT *
        FROM Game
        WHERE id = $id
      """.query[BowlingGame].option.transact(transactor)
  }

  def deleteGame(id: Long): IO[Either[GameNotFoundError, Unit]] = {
    sql"DELETE FROM Game WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        logger.info("deleted game {}",id)
        Right(())
      } else {
        logger.warn("delete : game not found {}",id)
        Left(GameNotFoundError(id))
      }
    }
  }


  private def parseJson(json: String): List[Roll] = {
    decode[List[Roll]](json).getOrElse(Nil)
  }

  private def parseJsonFrame(json: String): List[Frame] = {
    decode[List[Frame]](json).getOrElse(Nil)
  }

}
