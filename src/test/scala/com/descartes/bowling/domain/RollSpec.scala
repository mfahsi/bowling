package com.descartes.bowling.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import cats.implicits.toShow
import com.descartes.bowling.domain.RollScore.RS_STRIKE
import com.descartes.bowling.service.ScoringService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class RollSpec extends AnyFlatSpec with should.Matchers {
  val miss = Roll(0, None) //'-' not entered
  val miss_marked = Roll(0, Some('-')) //'-' not entered
  val full = Roll(10, None)
  val foul = Roll(0, Some('F'))
  val three = Roll(3, None)
  val seven = Roll(7, None)
  val split = Roll(5, Some('S'))

  "Show" should "show marker for X / - F" in {
    miss_marked.show should be("-")
    foul.show should be("F")
  }

  it should "show frame with foul as F" in {
    val frame = Frame(List(three, foul))
    frame.show should be("3 F")
  }

  it should "show pins and marker for split" in {
    split.show should be("5S")
  }

  it should "infer missing markers that are inferrable at roll level" in {
    miss.show should be("-")
  }

}
