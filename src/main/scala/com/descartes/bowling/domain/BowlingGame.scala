package com.descartes.bowling.domain

import cats.Show
import cats.implicits.toShow
import com.descartes.bowling.api.domain.FrameInfo.RollPosition
import com.descartes.bowling.domain.Roll.inferMarkers
import com.descartes.bowling.service.ScoringService
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder


object BowlingGame {
  implicit val encoder: Encoder[BowlingGame] = deriveEncoder[BowlingGame].mapJson(_.dropNullValues)
  implicit val gameShow: Show[BowlingGame] = Show.show(_.showGame)

  object dsl {

    def game(id: Option[Long] = None, player: Option[String] = None, lane: Option[String] = None, gameSet: Option[Long] = None)(frames: Frame*): BowlingGame = {
      BowlingGame(id, frames.toList, player, lane, gameSet)
    }

    def frame(rolls: Roll*): Frame = {
      Frame(rolls.toList)
    }

    def rolls(rolls: Int*): Frame =  Frame(rolls.map(Roll(_)).toList)

    def rollsN(repeat: Int)(rolls: Roll*): List[Frame] = frameN(repeat)(frame(rolls: _*))

    def frameN(repeat: Int)(f: Frame): List[Frame] = List.fill(repeat)(f)


    def roll(pins: Int, marker: Option[Char] = None): Roll = {
      Roll(pins, marker)
    }
  }

}

case class BowlingGame(id: Option[Long], frames: List[Frame] = List(), player: Option[String]=None, lane: Option[String]=None, gameSet: Option[Long]=None) {

  def pins(framePos: Int, rollPost: RollPosition): Option[Int] = frames.lift(framePos).flatMap(_.pins(rollPost))

  def marker(framePos: Int, rollPost: RollPosition): Option[Option[Char]] = frames.lift(framePos).flatMap(_.marker(rollPost))

  def roll(framePos: Int, rollPost: RollPosition): Option[Roll] = frame(framePos).flatMap(_.roll(rollPost))
  def frame(framePos: Int): Option[Frame] = frames.lift(framePos)


  def withMark(framePos: Int, newMarker: Char): BowlingGame = {
    val updatedFrames = frames.zipWithIndex.map {
      case (frame, index) if index == framePos => frame.withMark(0, newMarker)
      case (frame, _) => frame
    }
    this.copy(frames = updatedFrames)
  }

  def showGame: String = toString

  override def toString: String = {
    val nonEmptyFields = productIterator
      .zip(productElementNames)
      .collect {
        case (value: Option[_], name) if value.isDefined => s"$name=${value.get}"
        case (value, name) if value != null && !value.isInstanceOf[Option[_]] => s"$name=$value"
      }
    s"Game(${nonEmptyFields.mkString(", ")})"
  }

  def addedRoll(aRoll: Roll): BowlingGame = {
    if (isComplete) {
      throw new IllegalStateException("Game is complete already")
    }
    val roll = inferMarkers(aRoll)
    val isTenthFrame = frames.length == 10

    if (frames.isEmpty || frames.last.isComplete(isTenthFrame)) {
      val newFrame = Frame(List(roll))
      this.copy(frames = frames :+ newFrame)
    } else {
      val newLastFrame = Frame(frames.last.rolls :+ roll)
      this.copy(frames = frames.init :+ newLastFrame)
    }
  }

  def isComplete: Boolean = {
    if (frames.length < 10) {
      false
    } else {
      val lastFrame = frames.last
      val lastFrameRolls = lastFrame.rolls
      if (lastFrame.isStrike()) { // Strike
        lastFrameRolls.length == 3
      } else if (lastFrame.isSpare()) { // Spare
        lastFrameRolls.length == 3
      } else { // Open frame
        lastFrameRolls.length == 2
      }
    }
  }
}
