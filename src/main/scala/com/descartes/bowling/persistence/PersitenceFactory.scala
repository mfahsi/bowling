package com.descartes.bowling.persistence

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.descartes.bowling.config.{DbType, memory, postgres}
import doobie.Transactor
import doobie.hikari.HikariTransactor

object PersistenceFactory {

  def makeGameRepository(dbType: DbType, transactor: Transactor[IO]): GameRepository = {
    import DbType._
    dbType match {
      case `postgres` => new GameRepositoryPostgres(transactor)
      case `memory` => {
        val repo = new GameRepositoryH2(transactor)
        repo.initDatabase().apply(transactor).unsafeRunSync()
        repo
      }
    }
  }

}
