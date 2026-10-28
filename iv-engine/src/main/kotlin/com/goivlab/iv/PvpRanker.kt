package com.goivlab.iv

import java.math.BigDecimal

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
     * All 4,096 IV spreads using the highest eligible level under a CP cap.
     *
     * Rank is sequential, including ties. Exact decimal score arithmetic
     * avoids IEEE-754 rounding incorrectly splitting mathematically equal
     * stat products, which can shift published rank positions.
     *
     * Ties: higher effective Attack, then HP, CP, then IVs, descending.
     * Source-specific league legality and encounter IV floors are separate.
     */
    fun rank(base: BaseStats, cpCap: Int, levels: List<LevelMultiplier>): List<RankedSpread> {
        require(cpCap >= 10)
        require(levels.isNotEmpty())
        require(levels.map { it.level }.distinct().size == levels.size)
        val orderedLevels = levels.sortedBy { it.level }
        require(orderedLevels.zipWithNext().all { (a, b) -> a.multiplier < b.multiplier })

        data class Candidate(
            val iv: IvSpread,
            val level: Double,
            val stats: CalculatedStats,
            val exactScore: BigDecimal
        )

        val squaredCpms = orderedLevels.associate { level ->
            level.level to BigDecimal.valueOf(level.multiplier).pow(2)
        }
        val candidates = ArrayList<Candidate>(4096)
        for (a in 0..15) for (d in 0..15) for (s in 0..15) {
            val iv = IvSpread(a, d, s)
            val best = orderedLevels.asReversed().firstNotNullOfOrNull { level ->
                val stats = IvCalculator.calculate(base, iv, level.multiplier)
                if (stats.cp > cpCap) null else {
                    val numerator = BigDecimal.valueOf((base.attack + a).toLong())
                        .multiply(BigDecimal.valueOf((base.defense + d).toLong()))
                        .multiply(BigDecimal.valueOf(stats.hp.toLong()))
                    Candidate(iv, level.level, stats, numerator.multiply(squaredCpms.getValue(level.level)))
                }
            }
            if (best != null) candidates.add(best)
        }

        val sorted = candidates.sortedWith(
            compareByDescending<Candidate> { it.exactScore }
                .thenByDescending { it.stats.attack }
                .thenByDescending { it.stats.hp }
                .thenByDescending { it.stats.cp }
                .thenByDescending { it.iv.attack }
                .thenByDescending { it.iv.defense }
                .thenByDescending { it.iv.stamina }
        )
        return sorted.mapIndexed { index, item ->
            RankedSpread(item.iv, index + 1, item.level, item.stats.cp, item.exactScore.toDouble())
        }
    }
}
