import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.descartes.bowling.config.{DatabaseConfig, DbType, memory, postgres}
import com.descartes.bowling.persistence.{GameRepository, GameRepositoryH2, GameRepositoryPostgres, PersistenceFactory}
import doobie.{ExecutionContexts, Transactor}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers



class PersistenceFactorySpec extends AnyFlatSpec with Matchers {

  val postgresConf = DatabaseConfig(
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/test_db",
    user = "user",
    password = "password",
    2,
    None
  )
  val h2config = postgresConf.copy(driver="org.h2.Driver")
  val ec = ExecutionContexts.fixedThreadPool[IO](1).use(resource => IO.pure(resource)).unsafeRunSync()

  "PersistenceFactory" should "create a GameRepositoryPostgres for postgres DbType" in {

    val transactor: Transactor[IO] = GameRepository.transactor(postgresConf, ec).use(resource => IO.pure(resource)).unsafeRunSync()

    val repository = PersistenceFactory.makeGameRepository(postgres, transactor)
    repository shouldBe a[GameRepositoryPostgres]
  }

  it should "create a GameRepositoryH2 for memory DbType" in {
    val h2Config = DatabaseConfig(
      driver = "org.h2.Driver",
      url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // In-memory H2 database
      user = "sa", // H2 default username
      password = "",
      2,
      None
    )
    val transactor: Transactor[IO] = GameRepository.transactor(h2Config, ec).use(resource => IO.pure(resource)).unsafeRunSync()

    val repository = PersistenceFactory.makeGameRepository(memory, transactor)
    repository shouldBe a[GameRepositoryH2]
  }
}
