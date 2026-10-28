package com.goivlab.iv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PvpokeCpmTest {
    @Test fun knownLevelAnchors() {
        assertEquals(0.0939999967813491, PvpokeCpm.at(1.0))
        assertEquals(0.790300011634826, PvpokeCpm.at(40.0))
        assertEquals(0.840300023555755, PvpokeCpm.at(50.0))
        assertEquals(0.845300018787384, PvpokeCpm.at(51.0))
    }

    @Test fun halfLevelsAndLevelCaps() {
        assertEquals(101, PvpokeCpm.levels(51.0).size)
        assertEquals(99, PvpokeCpm.levels(50.0).size)
        assertEquals(0.135137430784308, PvpokeCpm.at(1.5))
        assertTrue(PvpokeCpm.levels().zipWithNext().all { (a, b) -> a.multiplier < b.multiplier })
        assertFailsWith<IllegalArgumentException> { PvpokeCpm.at(23.25) }
        assertFailsWith<IllegalArgumentException> { PvpokeCpm.at(51.5) }
    }

    @Test fun independentBulbasaurCpGoldenAnchor() {
        // GO Hub checker id=1 shows Bulbasaur 15/15/15 at L50 -> 1260 CP / 120 HP.
        val s = IvCalculator.calculate(BaseStats(118, 111, 128), IvSpread(15, 15, 15), PvpokeCpm.at(50.0))
        assertEquals(1260, s.cp)
        assertEquals(120, s.hp)
    }
}
