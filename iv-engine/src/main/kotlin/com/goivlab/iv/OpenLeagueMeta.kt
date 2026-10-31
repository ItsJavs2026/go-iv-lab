package com.goivlab.iv

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Fixed PvPoke Open League simulation ratings. Distinct from an IV spread rank. */
data class LeagueBattleRating(val league: String, val rank: Int, val score: Double)

class OpenLeagueMeta private constructor(
    private val ratings: Map<String, Map<String, LeagueBattleRating>>
) {
    fun get(league: String, speciesId: String): LeagueBattleRating? =
        ratings[league]?.get(speciesId)

    companion object {
        fun parse(text: String): OpenLeagueMeta {
            val root = Json.parseToJsonElement(text).jsonObject
            require(root.getValue("schemaVersion").jsonPrimitive.int == 1)
            require(root.getValue("sourceCommit").jsonPrimitive.content ==
                "d98f52174e01f8f67414bc6973599788d098c10e")
            val leagues = root.getValue("leagues").jsonObject
            require(leagues.keys == setOf("LC", "GL", "UL", "ML"))
            val output = leagues.mapValues { (league, rows) ->
                val result = linkedMapOf<String, LeagueBattleRating>()
                for (row in rows.jsonArray) {
                    val obj = row.jsonObject
                    val id = obj.getValue("id").jsonPrimitive.content
                    val rank = obj.getValue("rank").jsonPrimitive.int
                    val score = obj.getValue("score").jsonPrimitive.double
                    require(id.isNotBlank() && rank > 0 && score.isFinite() && score in 0.0..100.0)
                    require(result.put(id, LeagueBattleRating(league, rank, score)) == null) {
                        "Duplicate $league rating for $id"
                    }
                }
                result.toMap()
            }
            return OpenLeagueMeta(output)
        }
    }
}
