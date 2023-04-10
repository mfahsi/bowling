package com.descartes.bowling

import com.descartes.bowling.api.domain.RollInfo
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

package object domain {

  val STRIKE = Some('X')
  val SPARE = Some('/')
  val MISS = Some('-')
  val FOUL = Some('F')
  val SPLIT = Some('S')


  def rollShow(r: Roll, tenIsStrik: Boolean)(acc: Int = 0): Char = {
    r.pins match {
      case 10 => if (tenIsStrik) 'X' else '/'
      case _ => if (acc + r.pins == 10) '/' else {
        r.rollScore()
      }
    }
  }

  object jsonDerivation {
    implicit val customConfig: Configuration = Configuration.default.withDefaults

    implicit val rollInfoEncoder: Encoder.AsObject[RollInfo] = deriveConfiguredEncoder[RollInfo]
  }
}