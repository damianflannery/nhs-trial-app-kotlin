package com.nhstrial.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.username").getString()
    val pass = environment.config.property("database.password").getString()

    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        }
    val dataSource = HikariDataSource(hikariConfig)

    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate()

    Database.connect(dataSource)
}
