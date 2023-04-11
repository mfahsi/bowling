package com.descartes.bowling.api.domain

import com.descartes.bowling.domain.{DomainError, GameNotFoundError, IncompleteGameScoreAttempt, RollAttemptedGameComplete}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

/** ** API protocol wrapper : unictity tokens, security fields, error ...etc *** */

sealed trait ApiError {
}

case class ApiDomainError(domainError: DomainError) extends ApiError {
  def asFlatApiLayer: ApiLayerError = domainError match {
    case x: GameNotFoundError => ApiLayerError("Requested Game Not found", x.reference())
    case x: IncompleteGameScoreAttempt => ApiLayerError("Can't provide a final score on a pending game", x.reference())
    case x: RollAttemptedGameComplete => ApiLayerError("Can't roll on a complete game", x.reference())
    case de: DomainError => ApiLayerError(de.errorMessage(), de.reference())
  }
}

case class ApiLayerError(error: String, ref: Option[String] = None) extends ApiError

object ApiLayerError {
  implicit val encoder: Encoder[ApiLayerError] = deriveEncoder[ApiLayerError]
}

object ApiDomainError {
  implicit val encoder: Encoder[ApiDomainError] =
    ApiLayerError.encoder.contramap[ApiDomainError](_.asFlatApiLayer)
}



