package fr.fritsch.betclic.dao

import fr.fritsch.betclic.database.DatabaseSingleton.dbQuery
import fr.fritsch.betclic.models.Player
import fr.fritsch.betclic.models.Players
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*

class DAOFacadeImpl : DAOFacade {
    private fun resultRowToPlayer(row: ResultRow) = Player(
        id = row[Players.id],
        username = row[Players.username],
        score = row[Players.score],
        rank = row[Players.rank]
    )

    override suspend fun allPlayersSortedByScore(sortOrder: SortOrder?): List<Player> = dbQuery {
        if (sortOrder == SortOrder.ASC) {
            Players.selectAll().sortedBy { row ->
                row[Players.score]
            }.map(::resultRowToPlayer)
        } else {
            Players.selectAll().sortedByDescending { row ->
                row[Players.score]
            }.map(::resultRowToPlayer)
        }
    }

    override suspend fun player(id: Int): Player? = dbQuery {
        Players
            .select { Players.id eq id }
            .map(::resultRowToPlayer)
            .singleOrNull()
    }

    override suspend fun addNewPlayer(username: String, score: Int, rank: Int): Player? = dbQuery {
        val insertStatement = Players.insert {
            it[Players.username] = username
            it[Players.score] = score
            it[Players.rank] = rank
        }

        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPlayer)
    }

    override suspend fun editPlayer(id: Int, username: String, score: Int, rank: Int): Boolean = dbQuery {
        Players.update({ Players.id eq id }) {
            it[Players.username] = username
            it[Players.score] = score
            it[Players.rank] = rank
        } > 0
    }

    override suspend fun deleteAllPlayers(): Boolean = dbQuery {
        Players.deleteAll() > 0
    }

    override suspend fun processRanks() = dbQuery {
        val players = playerRepository.allPlayersSortedByScore(SortOrder.DESC)
        var rank = 1;

        for (player in players) {
            if (player.rank != rank) {
                playerRepository.editPlayer(player.id, player.username, player.score, rank);
            }
            ++rank;
        }
    }
}

val playerRepository: DAOFacade = DAOFacadeImpl().apply {
    runBlocking {
        // Populate database
        if (allPlayersSortedByScore(null).isEmpty()) {
            addNewPlayer("OG0", 0, 4)
            addNewPlayer("OG33", 33, 3)
            addNewPlayer("OG666", 666, 2)
            addNewPlayer("OG9000", 9000, 1)
        }
    }
}