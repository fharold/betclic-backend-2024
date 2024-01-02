package fr.fritsch.betclic.models

import org.jetbrains.exposed.sql.Table

object Players: Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 128).uniqueIndex()
    val score = integer("score")
    val rank = integer("rank")

    override val primaryKey = PrimaryKey(id)
}