package com.descartes.bowling.persistence

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.descartes.bowling.config.{AppConfig, memory}
import com.descartes.bowling.domain.BowlingGame.dsl._
import com.descartes.bowling.domain._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory

class GameRepositorySpec extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  val logger = LoggerFactory.getLogger(getClass)

  /** *SET in memory mode ** */
  val repository = AppConfig.makeCustomRepository.run(memory).use(sys => IO.pure(sys)).unsafeRunSync()
  val transactor = repository.transactor()

  test("createServer and read a game by id") {
    val g = game(None, Some("Chris Jhon"), Some("lane 2"))(frame(roll(5), roll(3, Some('S'))))
    val gameCreated = repository.insert(g).unsafeRunSync()
    val result = repository.findById(gameCreated).unsafeRunSync()
    result.isEmpty should be(false)
    result.get shouldBe BowlingGame(Some(gameCreated), player = Some("Chris Jhon"), lane = Some("lane 2"), frames = g.frames)
  }

}
