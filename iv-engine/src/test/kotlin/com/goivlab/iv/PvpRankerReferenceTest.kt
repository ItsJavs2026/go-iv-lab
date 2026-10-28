package com.goivlab.iv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PvpRankerReferenceTest {
    // Independent published reference:
    // https://db.pokemongohub.net/tools/pvp-iv-checker?id=1
    // Bulbasaur / Great League / cap 1500 / max level 50 / 0-IV floor.
    // Checked 2026-10-28.
    @Test fun bulbasaurGreatLeagueGoldenRanks() {
        val ranks = PvpRanker.rank(
            BaseStats(118, 111, 128), 1500, PvpokeCpm.levels(50.0)
        )
        assertEquals(4096, ranks.size)
        assertEquals(IvSpread(15, 15, 15), ranks.first().iv)
        assertEquals(1260, ranks.first().cp)
        assertEquals(50.0, ranks.first().level)
        val lowAttack = ranks.single { it.iv == IvSpread(0, 15, 15) }
        assertEquals(770, lowAttack.rank)
        assertEquals(1118, lowAttack.cp)
        assertEquals(50.0, lowAttack.level)
        assertTrue(ranks.zipWithNext().all { (a, b) -> a.rank + 1 == b.rank })
    }
}
