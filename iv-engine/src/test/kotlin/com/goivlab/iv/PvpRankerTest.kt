package com.goivlab.iv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class PvpRankerTest {
    private val levels = listOf(
        LevelMultiplier(1.0, 0.1),
        LevelMultiplier(2.0, 0.2),
        LevelMultiplier(3.0, 0.3)
    )

    @Test fun enumeratesEverySpreadWhenEligible() {
        val ranks = PvpRanker.rank(BaseStats(100, 100, 100), 1500, levels)
        assertEquals(4096, ranks.size)
        assertEquals(1, ranks.first().rank)
        assertEquals(15, ranks.first().iv.attack)
        assertEquals(15, ranks.first().iv.defense)
        assertEquals(15, ranks.first().iv.stamina)
    }

    @Test fun rankOrderingIsNonIncreasing() {
        val ranks = PvpRanker.rank(BaseStats(200, 180, 160), 100, levels)
        assertTrue(ranks.zipWithNext().all { (a, b) -> a.statProduct >= b.statProduct })
        assertTrue(ranks.all { it.cp <= 100 })
    }

    @Test fun rejectsInvalidLevelTables() {
        assertFailsWith<IllegalArgumentException> {
            PvpRanker.rank(BaseStats(100, 100, 100), 1500,
                listOf(LevelMultiplier(1.0, 0.3), LevelMultiplier(2.0, 0.2)))
        }
    }
}
