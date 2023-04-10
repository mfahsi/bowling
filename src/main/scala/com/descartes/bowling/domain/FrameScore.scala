package com.descartes.bowling.domain

import cats.Show

case class FrameScore(total: Int, rolls: List[RollScore])

object FrameScore {
  implicit val rollScoreShow: Show[FrameScore] = Show.show { fs =>
    s"score ${fs.total} ${fs.rolls.mkString(" ")}"
  }
}
