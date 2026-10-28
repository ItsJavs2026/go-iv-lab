package com.goivlab.iv

data class LevelMultiplier(val level: Double, val multiplier: Double) {
    init {
        require(level.isFinite() && level > 0)
        require(multiplier.isFinite() && multiplier > 0)
    }
}

data class RankedSpread(
    val iv: IvSpread,
    val rank: Int,
    val level: Double,
    val cp: Int,
    val statProduct: Double
)

object PvpRanker {
    /**
     * Enumerates all 4096 spreads, selecting the highest eligible stat product
     * at a supplied set of levels under the league CP cap.
     *
     * No bundled level multipliers: callers must provide a separately verified
     * level table. Ranks are competition ranks for equal stat products.
     */
    fun rank(
        base: BaseStats,
        cpCap: Int,
        levels: List<LevelMultiplier>
    ): List<RankedSpread> {
        require(cpCap >= 10)
        require(levels.isNotEmpty())
        require(levels.map { it.level }.distinct().size == levels.size)
        val orderedLevels = levels.sortedBy { it.level }
        require(orderedLevels.zipWithNext().all { (a, b) -> a.multiplier < b.multiplier })

        data class Candidate(val iv: IvSpread, val level: Double, val cp: Int, val product: Double)
        val candidates = ArrayList<Candidate>(4096)
        for (a in 0..15) for (d in 0..15) for (s in 0..15) {
            val iv = IvSpread(a, d, s)
            val best = orderedLevels.asSequence()
                .map { level -> level to IvCalculator.calculate(base, iv, level.multiplier) }
                .filter { (_, stats) -> stats.cp <= cpCap }
                .maxWithOrNull(compareBy<Pair<LevelMultiplier, CalculatedStats>> { it.second.statProduct }
                    .thenBy { it.first.level })
            if (best != null) {
                candidates.add(Candidate(iv, best.first.level, best.second.cp, best.second.statProduct))
            }
        }
        val sorted = candidates.sortedWith(
            compareByDescending<Candidate> { it.product }
                .thenBy { it.iv.attack }.thenByDescending { it.iv.defense }
                .thenByDescending { it.iv.stamina }
        )
        var previous: Double? = null
        var currentRank = 0
        return sorted.mapIndexed { index, item ->
            if (previous == null || item.product != previous) currentRank = index + 1
            previous = item.product
            RankedSpread(item.iv, currentRank, item.level, item.cp, item.product)
        }
    }
}
