package com.descartes.bowling.config

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.descartes.bowling.persistence.{GameRepositoryH2, GameRepositoryPostgres}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class AppConfigSpec extends AnyFlatSpec with should.Matchers {

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  "DB config" should "be able to load separately via makeDb Reader and Resource with injection" in {
    val db1 = AppConfig.makeDb.run(memory).use(config => IO.pure(config)).unsafeRunSync()
    db1.user should be("")
    val db2 = AppConfig.makeDb.run(postgres).use(config => IO.pure(config)).unsafeRunSync()
    db2.user should be("postgres")
  }
  it should "inject H2 database as well with no change in the API" in {
    val db2 = AppConfig.makeDb.run(postgres).use(config => IO.pure(config)).unsafeRunSync()
    db2.user should be("postgres")
  }

  "Api config" should "be able to load separately via makeDb Reader and Resource with injection" in {
    val api = AppConfig.makeApi.use(config => IO.pure(config)).unsafeRunSync()
    api.port should be(8080)
    api.host should be("0.0.0.0")
  }

  "Repository Config" should "can load command config on H2" in {
    val repo = AppConfig.makeCustomRepository.run(memory).use(config => IO.pure(config)).unsafeRunSync()
    repo  shouldBe a [GameRepositoryH2]
  }

  "Application Config " should "can load with postgres database file" in {
    val app = AppConfig.makeApp.run(postgres).use(config => IO.pure(config)).unsafeRunSync()
    app.database.user should be("postgres")
    app.database.driver should be("org.postgresql.Driver")
    app.server.port should be(8080)
  }

  it should "can load with H2 database file" in {
    val appmem = AppConfig.makeApp.run(memory).use(config => IO.pure(config)).unsafeRunSync()
    appmem.database.user should be("")
    appmem.database.driver should be("org.h2.Driver")
    appmem.server.port should be(8080)
  }

}
