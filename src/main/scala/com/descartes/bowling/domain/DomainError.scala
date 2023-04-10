package com.descartes.bowling.domain

class DomainError(message:String, ref:Option[String]=None){
  def reference()=ref
  def errorMessage() = message
}

case class GameNotFoundError(gameId:Long) extends DomainError("Game not found",Some(gameId.toString))
case class IncompleteGameScoreAttempt(message :String,gameId:Long) extends DomainError(message,Some(gameId.toString))
