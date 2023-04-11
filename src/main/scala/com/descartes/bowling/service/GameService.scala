package com.descartes.bowling.service

import cats.effect.IO
import com.descartes.bowling.domain.{BowlingGame, DomainError, GameNotFoundError, Roll}
import com.descartes.bowling.persistence.GameRepository
import org.slf4j.LoggerFactory

class GameService(gameRepository:GameRepository) {
  val logger = LoggerFactory.getLogger(getClass)

  def roll(gameId: Long, newRoll: Roll): IO[Either[DomainError, BowlingGame]] = {
    for {
      maybeRolled <- gameRepository.findById(gameId).map(r => {
        val maybeG = r.map(g => {
          logger.info("roll {} to game {}",newRoll,g)
          g.addedRoll(newRoll)
        })
        maybeG
      })
      saved <- gameRepository.updateGameOpt(gameId, maybeRolled)
    } yield saved
  }

}
