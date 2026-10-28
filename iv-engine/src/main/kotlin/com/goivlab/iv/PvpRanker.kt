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
     * Evaluates all 4,096 IV spreads using a provided level table.
     *
     * Each IV spread uses the highest eligible level under the CP cap.
     * Results use sequential ranks (1, 2, 3...) even for tied stat products.
     * Tie-breaker: effective Attack, HP, CP, then IVs (descending).
     *
     * This is a documented ranking policy, NOT proof of exact parity
     * against third-party calculators for every species and form.
     */
    fun rank(base: BaseStats, cpCap: Int, levels: List<LevelMultiplier>): List<RankedSpread> {
        require(cpCap >= 10)
        require(levels.isNotEmpty())
        require(levels.map { it.level }.distinct().size == levels.size)
        val orderedLevels = levels.sortedBy { it.level }
        require(orderedLevels.zipWithNext().all { (a, b) -> a.multiplier < b.multiplier })

        data class Candidate(val iv: IvSpread, val level: Double, val stats: CalculatedStats)
        val candidates = ArrayList<Candidate>(4096)

        for (a in 0..15) for (d in 0..15) for (s in 0..15) {
            val iv = IvSpread(a, d, s)
            // CPM and CP strictly increase with level except rounding plateaus;
            // the highest eligible level maximizes or preserves stat product.
            val best = orderedLevels.asReversed().firstNotNullOfOrNull { level ->
                val stats = IvCalculator.calculate(base, iv, level.multiplier)
                if (stats.cp <= cpCap) Candidate(iv, level.level, stats) else null
            }
            if (best != null) candidates.add(best)
        }

        val sorted = candidates.sortedWith(
            compareByDescending<Candidate> { it.stats.statProduct }
                .thenByDescending { it.stats.attack }
                .thenByDescending { it.stats.hp }
                .thenByDescending { it.stats.cp }
                .thenByDescending { it.iv.attack }
                .thenByDescending { it.iv.defense }
                .thenByDescending { it.iv.stamina }
        )
        return sorted.mapIndexed { index, item ->
            RankedSpread(item.iv, index + 1, item.level, item.stats.cp, item.stats.statProduct)
        }
    }
}
