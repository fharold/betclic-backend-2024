package fr.fritsch.betclic.dao

import fr.fritsch.betclic.models.Player
import org.jetbrains.exposed.sql.SortOrder

interface DAOFacade {
    suspend fun allPlayersSortedByScore(sortOrder: SortOrder?): List<Player>
    suspend fun player(id: Int): Player?
    suspend fun addNewPlayer(username: String, score: Int, rank: Int): Player?
    suspend fun editPlayer(id: Int, username: String, score: Int, rank: Int): Boolean
    suspend fun deleteAllPlayers(): Boolean
    suspend fun processRanks()
}