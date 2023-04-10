package com.descartes.bowling.persistence

import cats.effect.IO
import com.descartes.bowling.domain.{BowlingGame, GameNotFoundError}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import doobie.util.fragment
import org.slf4j.LoggerFactory

/** change JSON Type for frames on some queries only **/
/** TODO init script here instead of container, move to script **/

class GameRepositoryH2(transactor: Transactor[IO]) extends GameRepositoryPostgres(transactor)  {

  override def insert(game: BowlingGame): IO[Long] = {
    sql"""
        INSERT INTO Game (player, lane, frames)
        VALUES (${game.player},${game.lane},${game.frames})
      """.update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
  }

 override def updateGame(id: Long, g: BowlingGame): IO[Either[GameNotFoundError, BowlingGame]] = {
    sql"UPDATE Game SET frames = ${g.frames} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        logger.info("Game updated game {}" , g)
        Right(g.copy(id = Some(id)))
      } else {
        Left(GameNotFoundError(id))
      }
    }
  }

 override val dbInitScript : Option[fragment.Fragment] = Some(
   sql"""
     CREATE TABLE IF NOT EXISTS BowlingGroupGame (
      id serial NOT NULL PRIMARY KEY,
      players SMALLINT NULL,
      gameSetDate DATE NULL,
      lane SMALLINT NULL
    );
    CREATE TABLE IF NOT EXISTS Game (
    id serial NOT NULL PRIMARY KEY,
    frames TEXT NULL,
    player TEXT NULL,
    lane   TEXT NULL,
    gameDate DATE NULL,
    gameSetId integer NULL REFERENCES BowlingGroupGame(id) ON DELETE CASCADE
  );

    """)
 }

