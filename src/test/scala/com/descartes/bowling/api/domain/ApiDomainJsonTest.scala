package com.descartes.bowling.api.domain

import com.descartes.bowling.domain.{GameNotFoundError, MISS}
import io.circe.Decoder
import io.circe.literal.JsonStringContext
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ApiDomainJsonTest extends AnyFlatSpec with should.Matchers {

  import io.circe.syntax._

  val gameA = BowlingGameInfo(None, List())

  "circe custom config encoder" should "show remove nullvalues" in {
    RollInfo(3).asJson should be(json"""{"pins": 3}""")
  }

  "empty game json " should "show null id" in {
    gameA.asJson should be(json"""{ "frames":[] } """)
  }

  "frame encode " should "encode frame (tests also rolls encodding)" in {
    val r = FrameInfo(List(RollInfo(3, None, Some("c4j")), RollInfo(0, MISS)))
    val js = r.asJson
    val json = json"""{ "rolls" : [{ "pins":3,  "uid" : "c4j"},{ "pins":0, "marker": "-"}] }"""
    js shouldBe json
  }

  "GameInfo decode " should "encode frame (tests also rolls encodding)" in {

    val json = json"""{"id": null, "frames":[], "player" : null, "lane" : null, "gameSet": null } """
    Decoder[BowlingGameInfo].decodeJson(json).getOrElse(null) shouldBe (gameA)
  }

  "API error" should "generate valid json" in {
    ApiDomainError(GameNotFoundError(43443)).asJson shouldBe json"""{
                                                                    "error" : "Requested Game Not found",
                                                                     "ref" : "43443"
                                                                   }"""
  }

}
