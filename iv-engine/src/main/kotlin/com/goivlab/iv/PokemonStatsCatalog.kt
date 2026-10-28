package com.goivlab.iv

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class PokemonSpecies(val speciesId: String, val name: String, val baseStats: BaseStats)

/**
 * Pure offline parser for version-pinned GO stats. No network access, no credentials.
 * PvPoke's unique speciesId includes regional/Shadow form distinctions.
 */
class PokemonStatsCatalog private constructor(
    private val species: Map<String, PokemonSpecies>
) {
    val size: Int get() = species.size

    fun find(speciesId: String): PokemonSpecies? = species[speciesId]

    fun requireSpecies(speciesId: String): PokemonSpecies =
        species[speciesId] ?: throw IllegalArgumentException("Unknown species/form: $speciesId")

    companion object {
        fun parse(jsonText: String): PokemonStatsCatalog {
            val rows = Json.parseToJsonElement(jsonText).jsonArray
            require(rows.isNotEmpty()) { "Missing Pokémon records" }
            val parsed = LinkedHashMap<String, PokemonSpecies>(rows.size)
            for (row in rows) {
                val obj = row.jsonObject
                val id = obj.getValue("speciesId").jsonPrimitive.content
                val name = obj.getValue("name").jsonPrimitive.content
                require(id.isNotBlank() && name.isNotBlank())
                val base = obj.getValue("base").jsonObject
                val stats = BaseStats(
                    base.getValue("attack").jsonPrimitive.int,
                    base.getValue("defense").jsonPrimitive.int,
                    base.getValue("stamina").jsonPrimitive.int
                )
                require(parsed.put(id, PokemonSpecies(id, name, stats)) == null) {
                    "Duplicate species/form ID: $id"
                }
            }
            return PokemonStatsCatalog(parsed.toMap())
        }
    }
}
