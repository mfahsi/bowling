package com.descartes.bowling.domain

class DomainError(message:String, ref:Option[String]=None){
  def reference()=ref
  def errorMessage() = message
}

case class GameNotFoundError(gameId:Long) extends DomainError("Game not found",Some(gameId.toString))
case class IncompleteGameScoreAttempt(gameId:Long) extends DomainError("score not available when game is pending",Some(gameId.toString))
case class RollAttemptedGameComplete(gameId:Long) extends DomainError("Game is complete already",Some(gameId.toString))
