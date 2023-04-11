package com.descartes.bowling.domain

import cats.Show
import cats.implicits.toShow
import com.descartes.bowling.api.domain.FrameInfo.RollPosition
import io.circe.Encoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.generic.semiauto.deriveEncoder
import cats.Show
import cats.syntax.show._

trait FrameLike {
  def pins(i: RollPosition): Option[Int]

//  def marker(i: RollPosition): Option[Option[Char]]
}

object Frame {
  implicit val encoder: Encoder[Frame] = deriveEncoder[Frame].mapJson(_.dropNullValues)
  implicit val frameShow: Show[Frame] = Show.show(_.showFrame())
}
case class Frame(rolls: List[Roll]) extends FrameLike {
  def isSpare(): Boolean = !isStrike() && ! rolls.tail.isEmpty && rolls.take(2).map(_.pins).sum == 10

  def isStrike(): Boolean = rolls.head.isPerfectRoll

  def isComplete(isTenthFrame: Boolean): Boolean = {
    val isStrike = rolls.headOption.exists(_.pins == 10)
    val isSpare = rolls.length == 2 && rolls.map(_.pins).sum == 10

    if (isTenthFrame) {
      if (isStrike) {
        rolls.length == 3
      } else if (isSpare) {
        rolls.length == 3
      } else {
        rolls.length == 2
      }
    } else {
      rolls.length == 2 || isStrike
    }
  }

   def inferSplitMarker():Frame ={
    if(isSpare()) {
      if (!roll(1).get.isPerfectRoll) {
        Frame(rolls.updated(1, rolls(1).withMark(SPARE.get)))
      } else {
        this
      }
    }else{
      this
    }

  }

  def showFrame(): String = {
    val frame= inferSplitMarker()
    s"${frame.rolls.map(_.show).mkString(" ")}"
  }

  def pins(i: RollPosition):Option[Int] = roll(i).map(_.pins)
  def marker(i: RollPosition):Option[Option[Char]] = roll(i).map(_.marker)
  def roll(rollPost: RollPosition):Option[Roll] = rolls.lift(rollPost)
  override def toString: String = showFrame()
}
