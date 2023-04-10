package com.descartes.bowling.domain

import io.circe.Decoder
import io.circe.literal.JsonStringContext
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GameJsonTest extends AnyFlatSpec with should.Matchers {

  import io.circe.generic.auto._
  import io.circe.syntax._

  "empty game encode json then decode " should "show null id" in {
    val game = BowlingGame(None, List())
    val json = game.asJson
    json should be(json"""{"frames":[]}""")
    Decoder[BowlingGame].decodeJson(json).getOrElse(null) shouldBe (game)
  }

  "roll decode " should "show tolerate absense of marker" in {
    val game = Roll(5)
    val json = json"""{ "gameId": 123, "pins":5}"""
    Decoder[Roll].decodeJson(json).getOrElse(null) should be(game)
  }

  "roll decode " should "show tolerate null value of marker" in {
    val r = Roll(5)
    val json = json"""{ "gameId": 123, "pins":5, "marker": null}"""
    Decoder[Roll].decodeJson(json).getOrElse(null) should be(r)
  }

  "frame decode " should "deserialize frame (tests also rolls decoding)" in {
    val r = Frame(List(Roll(3, None), Roll(0, MISS)))
    val json = json"""{ "rolls" : [{ "pins":3, "marker": null},{ "pins":0, "marker": "-"}] }"""
    Decoder[Frame].decodeJson(json).getOrElse(null) should be(r)
  }

  "frame encode " should "encode frame (tests also rolls encodding)" in {
    val r = Frame(List(Roll(3, None), Roll(0, MISS)))
    val js = r.asJson
    val json = json"""{ "rolls" : [{ "pins":3, "marker": null},{ "pins":0, "marker": "-"}] }"""
    json shouldBe js
  }


}
