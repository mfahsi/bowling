package com.descartes.bowling.config

case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int, initScript:Option[String])

case class GameDatabase(database: DatabaseConfig)

