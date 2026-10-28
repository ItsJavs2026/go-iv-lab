package com.goivlab.iv

data class PvpIvLookupResult(
    val species: PokemonSpecies,
    val requestedIvs: IvSpread,
    val rank: Int,
    val eligibleSpreads: Int,
    val level: Double,
    val cp: Int,
    val statProductPercent: Double
)

/**
 * Offline user-facing lookup. Rank means within-species stat product rank,
 * never overall Pokémon battle strength or a matchup rating.
 */
class PvpIvLookup(private val catalog: PokemonStatsCatalog) {
    fun check(
        speciesId: String,
        ivs: IvSpread,
        cpCap: Int = 1500,
        maxLevel: Double = 50.0
    ): PvpIvLookupResult {
        val species = catalog.requireSpecies(speciesId)
        val ranks = PvpRanker.rank(species.baseStats, cpCap, PvpokeCpm.levels(maxLevel))
        require(ranks.isNotEmpty()) { "No eligible IV spreads at this CP limit" }
        val match = ranks.firstOrNull { it.iv == ivs }
            ?: throw IllegalArgumentException("Requested spread is ineligible at this CP cap")
        return PvpIvLookupResult(
            species = species,
            requestedIvs = ivs,
            rank = match.rank,
            eligibleSpreads = ranks.size,
            level = match.level,
            cp = match.cp,
            statProductPercent = 100.0 * match.statProduct / ranks.first().statProduct
        )
    }
}
