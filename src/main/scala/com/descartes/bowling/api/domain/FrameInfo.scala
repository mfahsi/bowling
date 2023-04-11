package com.descartes.bowling.api.domain

import com.descartes.bowling.api.domain.FrameInfo.RollPosition
import com.descartes.bowling.domain.FrameLike
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


case class FrameInfo(rolls: List[RollInfo]) extends FrameLike {
  def pins(i: RollPosition): Option[Int] = roll(i).map(_.pins)

  def roll(rollPost: RollPosition): Option[RollInfo] = rolls.lift(rollPost)
}

object FrameInfo {
  implicit val decoder: Decoder[FrameInfo] = deriveDecoder[FrameInfo]
  implicit val encoder: Encoder[FrameInfo] = deriveEncoder[FrameInfo].mapJson(_.dropNullValues)

  type RollPosition = Int  // 0 or 1 or 2
}