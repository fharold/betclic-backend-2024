package fr.fritsch.betclic

import fr.fritsch.betclic.database.DatabaseSingleton
import fr.fritsch.betclic.models.Player
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ThreadLocalRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerRouteTest {
    private fun initDb(environment: ApplicationEnvironment) {
        val jdbcURL = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        DatabaseSingleton.init(jdbcURL, user, password)
    }

    @Test
    fun testGetPlayerByInvalidId() = testApplication {
        application {
            initDb(environment)
        }
        client.get("/players/abcd").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetPlayerByExistingId() = testApplication {
        application {
            initDb(environment)
        }

        val response = client.submitForm(url = "/players",
            formParameters = parameters {
                append("name", "kitano${ThreadLocalRandom.current().nextInt()}")
            })
        assertEquals(HttpStatusCode.Created, response.status)

        val player: Player = Json.decodeFromString(response.bodyAsText())

        client.get("/players/${player.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testPlayerCreation() = testApplication {
        application {
            initDb(environment)
        }

        // Wipe all players from database
        client.delete("/players").apply {
            val response = client.submitForm(url = "/players",
                formParameters = parameters {
                    append("name", "kitano")
                })
            assertEquals(HttpStatusCode.Created, response.status)

            val player: Player = Json.decodeFromString(response.bodyAsText())
            assertEquals("kitano", player.username)
        }
    }

    @Test
    fun testPlayersDeletion() = testApplication {
        application {
            initDb(environment)
        }

        // Add a player to database
        val response = client.submitForm(url = "/players",
            formParameters = parameters {
                append("name", "kitano${ThreadLocalRandom.current().nextInt()}")
            })
        assertEquals(HttpStatusCode.Created, response.status)

        // Check players count
        val playersListResponse = client.get("/players").bodyAsText()
        val playerList: Array<Player> = Json.decodeFromString(playersListResponse)
        assert(playerList.isNotEmpty())

        // Wipe players records
        assertEquals(HttpStatusCode.OK, client.delete("/players").status)

        // Check if players count equals zero
        val wipedPlayersListResponse = client.get("/players").bodyAsText()
        val wipedPlayerList: Array<Player> = Json.decodeFromString(wipedPlayersListResponse)
        assert(wipedPlayerList.isEmpty())
    }
}