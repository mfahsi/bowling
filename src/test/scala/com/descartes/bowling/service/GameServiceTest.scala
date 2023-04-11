package com.descartes.bowling.service

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.descartes.bowling.config.{AppConfig, memory}
import com.descartes.bowling.domain._
import doobie.util.transactor.Transactor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GameServiceTest extends AnyFlatSpec with should.Matchers {
  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val repository = AppConfig.makeCustomRepository.run(memory).use(sys => IO.pure(sys)).unsafeRunSync()
  val transactor: Transactor[IO] = repository.transactor()
  val gameservice = new GameService(repository)


  "roll should " should "add roll if game is not complete" in {
    val gid = repository.insert(BowlingGame(None, List())).unsafeRunSync()
    gameservice.roll(gid, Roll(2, Some('S'))).unsafeRunSync()
    val result = gameservice.roll(gid, Roll(10)).unsafeRunSync()
    result.isRight should be(true)
    result.getOrElse(null).roll(0, 1) should be(Some(Roll(10, Some('X')))) //X inferred
    result.getOrElse(null).roll(0, 0) should be(Some(Roll(2, Some('S'))))

  }
  it should "return RollAttemptedGameComplete if game is complete before the roll" in {
    val gid = repository.insert(BowlingGame(None, List(), Some("Ember"), Some("lane 2"))).unsafeRunSync()
    import com.descartes.bowling.domain.BowlingGame.dsl._
    val full10FrameGame = frameN(10)(rolls(2, 6))
    repository.updateGame(gid, BowlingGame(id = Some(gid), frames = full10FrameGame)).unsafeRunSync()
    gameservice.roll(gid, Roll(10)).unsafeRunSync().left.toOption.get shouldBe a [RollAttemptedGameComplete]
  }

  it should "return GameNotFoundError if game does not exist" in {
    gameservice.roll(134253, Roll(10)).unsafeRunSync() should be(Left(GameNotFoundError(134253)))
  }

}
