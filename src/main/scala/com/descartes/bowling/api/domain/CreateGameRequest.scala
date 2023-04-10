package com.descartes.bowling.api.domain

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class CreateGameRequest(game: BowlingGameInfo, uid: Option[String])

object CreateGameRequest {
  implicit val decoder: Decoder[CreateGameRequest] = deriveDecoder[CreateGameRequest]
}
