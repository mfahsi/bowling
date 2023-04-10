package com.descartes.bowling.api.domain

import com.descartes.bowling.domain.{DomainError, GameNotFoundError}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

/** ** API protocol wrapper : unictity tokens, security fields, error ...etc *** */

sealed trait ApiError {
}

case class ApiDomainError(domainError: DomainError) extends ApiError {
  def asFlatApiLayer: ApiLayerError = domainError match {
    case x: GameNotFoundError => ApiLayerError("Requested Game Not found", x.reference())
    case de: DomainError => ApiLayerError("Request failure", de.reference())
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



