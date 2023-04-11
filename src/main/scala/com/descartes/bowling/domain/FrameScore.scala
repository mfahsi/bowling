package com.descartes.bowling.domain

import cats.Show
import cats.implicits.toShow

case class FrameScore(total: Int, rolls: List[RollScore])

object FrameScore {
  implicit val rollScoreShow: Show[FrameScore] = Show.show { fs =>
    s"score ${fs.total} ${fs.rolls.map(_.show).mkString(" ")}"
  }
}
