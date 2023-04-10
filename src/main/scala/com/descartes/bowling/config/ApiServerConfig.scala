package com.descartes.bowling.config

case class BowlingApi(server: ServerConfig)

case class ServerConfig(host: String, port: Int)

case class DbTypeConfigW(storage: DbTypeConfig)

case class DbTypeConfig(db: DbType)



