package fr.fritsch.betclic

import fr.fritsch.betclic.database.DatabaseSingleton
import fr.fritsch.betclic.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jdbcURL = environment.config.property("postgres.url").getString()
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()

    DatabaseSingleton.init(jdbcURL, user, password)
    configureSerialization()
    configureRouting()
}
