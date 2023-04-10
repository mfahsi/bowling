lazy val commonSettings = Seq(
  name := "bowling",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator",
    "-Ymacro-annotations",
  ),
)
enablePlugins(
  JavaAppPackaging,
  DockerPlugin
)
Compile / mainClass := Some("com.descartes.bowling.ServerApp")
Docker / packageName := "fahsi/bowling-api"
dockerExposedPorts ++= Seq(8086)
dockerBaseImage := "openjdk:11.0.11-jre"

lazy val Http4sVersion = "0.23.12"

lazy val DoobieVersion = "1.0.0-RC2"
//val NewTypeVersion = "0.4.4"
lazy val H2Version = "2.1.214"

lazy val FlywayVersion = "9.2.0"

lazy val CirceVersion = "0.14.1"

lazy val PureConfigVersion = "0.17.1"

lazy val LogbackVersion = "1.2.11"

lazy val ScalaTestVersion = "3.2.13"

lazy val ScalaMockVersion = "5.2.0"

lazy val httpDependencies = Seq(
  "org.http4s"            %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"            %% "http4s-circe"        % Http4sVersion,
  "org.http4s"            %% "http4s-dsl"          % Http4sVersion
)

lazy val testDependencies = Seq(
  // "org.testcontainers" % "testcontainers" % "1.16.3" % Test,
  "org.testcontainers"    % "postgresql" % "1.16.3" % Test,
  "org.scalatest"         %% "scalatest" % ScalaTestVersion % "it,test",
  "org.scalamock"         %% "scalamock" % ScalaMockVersion % "test",
  "com.h2database"        %  "h2"                   % H2Version % Test,
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,

      "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

      "io.circe"              %% "circe-generic"        % CirceVersion,
      "io.circe"              %% "circe-literal"        % CirceVersion,
      "io.circe"              %% "circe-parser"         % CirceVersion,
      "io.circe"              %% "circe-optics"         % CirceVersion      % "it",
      "io.circe"              %% "circe-generic-extras" % CirceVersion,

      "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,

      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,
      "org.slf4j"             % "slf4j-api"             % "1.7.36",
    ) ++ httpDependencies ++ testDependencies



  )

fork in run := true
outputStrategy := Some(StdoutOutput)

enablePlugins(JavaAppPackaging, DockerPlugin)


resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Besu repository" at	"https://hyperledger.jfrog.io/artifactory/besu-maven/"
)

Test / parallelExecution := false