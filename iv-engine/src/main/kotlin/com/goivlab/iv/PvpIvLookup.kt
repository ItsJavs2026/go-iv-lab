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
 *
 * A small, synchronized in-memory cache makes subsequent IV changes for the
 * same Pokémon/league fast without storing anything outside the device.
 */
class PvpIvLookup(private val catalog: PokemonStatsCatalog) {
    private data class Key(val speciesId: String, val cpCap: Int, val maxLevel: Double)
    private val rankCache = object : LinkedHashMap<Key, List<RankedSpread>>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, List<RankedSpread>>?): Boolean {
            return size > 4
        }
    }

    fun check(
        speciesId: String,
        ivs: IvSpread,
        cpCap: Int = 1500,
        maxLevel: Double = 50.0
    ): PvpIvLookupResult {
        val species = catalog.requireSpecies(speciesId)
        val key = Key(speciesId, cpCap, maxLevel)
        val cached = synchronized(rankCache) { rankCache[key] }
        val ranks = cached ?: PvpRanker.rank(species.baseStats, cpCap, PvpokeCpm.levels(maxLevel)).also {
            synchronized(rankCache) { rankCache[key] = it }
        }
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
