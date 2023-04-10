package com.descartes.bowling

import cats.effect._
import com.descartes.bowling.api.GameRoutes
import com.descartes.bowling.config.{AppConfig, BowlingAppConfig, ServerConfig}
import com.descartes.bowling.persistence.{GameRepository, GameRepositoryPostgres, PersistenceFactory}
import com.descartes.bowling.service.GameService
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._

object HttpServer {

  def createServer(): IO[ExitCode] = {
    resources().use(createBoot)
  }

  private def resources(): Resource[IO, Resources] = {
    for {
      apiConfig <- AppConfig.makeApi
      gameRepoResource <- AppConfig.makeAppRepo
      gameRepo <- gameRepoResource
    } yield Resources(gameRepo, apiConfig)
  }

  private def createBoot(resources: Resources): IO[ExitCode] = {
    for {
         exitCode <- BlazeServerBuilder[IO]
        .bindHttp(resources.config.port, resources.config.host)
        .withHttpApp(new GameRoutes(new GameService(resources.repository),resources.repository).routes.orNotFound).serve.compile.lastOrError
    } yield exitCode
  }

  case class Resources(repository: GameRepository, config: ServerConfig)
}
