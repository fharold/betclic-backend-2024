package fr.fritsch.betclic.plugins

import fr.fritsch.betclic.dao.playerRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SortOrder

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/players") {
            get {
                val sortOrder: SortOrder? = call.request.queryParameters["sort"]?.let {
                    when (it) {
                        SortOrder.ASC.code -> SortOrder.ASC
                        SortOrder.DESC.code -> SortOrder.DESC
                        else -> SortOrder.ASC
                    }
                }

                call.respond(playerRepository.allPlayersSortedByScore(sortOrder))
            }

            get("{id?}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)
                playerRepository.player(id)?.let {
                    call.respond(it)
                } ?: run {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post {
                val formParameters = call.receiveParameters()
                val name = formParameters.getOrFail("name")
                val score = formParameters["score"]?.toIntOrNull() ?: 0

                try {
                    val player = playerRepository.addNewPlayer(name, score, 0)
                    playerRepository.processRanks()

                    player?.let {
                        playerRepository.player(it.id)?.let {rankedPlayer ->
                            call.respond(HttpStatusCode.Created, rankedPlayer)
                        }
                    }
                } catch (e: ExposedSQLException) {
                    // SQLState duplicate error code
                    if (e.sqlState == "23505") {
                        call.respond(HttpStatusCode.Conflict)
                    } else {
                        throw e
                    }
                }
            }

            put("{id}") {
                val formParameters = call.receiveParameters()
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respondText("Missing id", status = HttpStatusCode.BadRequest)
                val name = formParameters.getOrFail("name")
                val score = formParameters.getOrFail("score").toIntOrNull() ?: return@put call.respondText("Malformed body", status = HttpStatusCode.UnprocessableEntity)

                try {
                    playerRepository.editPlayer(id, name, score, 0)
                    playerRepository.processRanks()
                    call.respondRedirect("/players/${id}")
                } catch (e: ExposedSQLException) {
                    // SQLState duplicate error code
                    if (e.sqlState == "23505") {
                        call.respond(HttpStatusCode.Conflict)
                    } else {
                        throw e
                    }
                }
            }

            delete {
                playerRepository.deleteAllPlayers()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
