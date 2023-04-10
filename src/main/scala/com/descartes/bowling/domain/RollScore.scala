package com.descartes.bowling.domain

import cats.Show

object RollScore {
  val RS_STRIKE = RollScore(10,Some('X'))

  implicit val rollScoreShow: Show[RollScore] = Show.show { rollScore =>
      Roll(rollScore.pins,rollScore.marker).showRoll()
  }
}

case class RollScore(pins: Int, marker: Option[Char]) {
  def isPerfectRoll: Boolean = pins == 10
}
