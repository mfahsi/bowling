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

  test("insert a game") {
    val game = BowlingGame(None, List(Frame(List(Roll(5), Roll(3, Some('S'))))),Some("Jhon"), Some("Lane 3"))
    val gameId = repository.insert(game).unsafeRunSync()
    gameId should be > 0L
  }

  test("createServer and read a game by id") {
    val g = game(None, Some("Chris Jhon"), Some("lane 2"))(frame(roll(5), roll(3, Some('S'))))
    val gameCreated = repository.insert(g).unsafeRunSync()
    gameCreated should be  >= 1L
    val result = repository.findById(gameCreated).unsafeRunSync()
    result.isEmpty should be(false)
    result.get shouldBe BowlingGame(Some(gameCreated), player = Some("Chris Jhon"), lane = Some("lane 2"), frames = g.frames)
  }

  test("update game and roll") {
    val g = game(None, Some("Chris Jhon"), Some("lane 2"))(frame(roll(5), roll(3, Some('S'))))
    val gameCreated = repository.insert(g).unsafeRunSync()
    val result = repository.updateGame(gameCreated, g.copy(id=Some(gameCreated), frames=g.frames ++ List(rolls(4,4)))).unsafeRunSync()
    result.toOption.isDefined should be(true)
    result.toOption.get.frames.size should be (2)
    result.toOption.get.frames(0).pins(0).get should be (5)
   }

  test("delete a game") {
    val game = BowlingGame(None, List(Frame(List(Roll(5), Roll(3, Some('S'))))), Some("Jhon"), Some("Lane 3"))
    val gameId = repository.insert(game).unsafeRunSync()

    val deleteResult = repository.deleteGame(gameId).unsafeRunSync()
    deleteResult.isRight shouldBe true
  }

}
