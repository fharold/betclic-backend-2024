package fr.fritsch.betclic.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(val id: Int, val username: String, val score: Int, val rank: Int)