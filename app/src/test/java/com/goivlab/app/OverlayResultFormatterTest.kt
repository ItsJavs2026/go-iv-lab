package com.goivlab.app

import com.goivlab.iv.BaseStats
import com.goivlab.iv.IvSpread
import com.goivlab.iv.PokemonSpecies
import com.goivlab.iv.PvpIvLookupResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayResultFormatterTest {
    private val alolanDiglett = PvpIvLookupResult(
        species = PokemonSpecies("diglett_alolan", "Diglett (Alolan)", BaseStats(108, 136, 67)),
        requestedIvs = IvSpread(13, 14, 11),
        rank = 125,
        eligibleSpreads = 4096,
        level = 50.0,
        cp = 735,
        statProductPercent = 93.05
    )

    @Test fun preservesExactFormAndRankInFloatSummary() {
        assertEquals(
            "Diglett (Alolan) (diglett_alolan) · GL\nIV 13/14/11 · Rank #125/4096\nCP 735 · Lv 50.0\nStat product 93.05% of #1",
            OverlayResultFormatter.format(alolanDiglett, "GL")
        )
    }

    @Test fun rejectsUnrecognizedLeagueInsteadOfInventingRankContext() {
        assertThrows(IllegalArgumentException::class.java) {
            OverlayResultFormatter.format(alolanDiglett, "FANTASY")
        }
    }

    @Test fun sameRankDataSupportsMultipleSupportedLeagues() {
        for (league in listOf("LC", "GL", "UL", "ML")) {
            val summary = OverlayResultFormatter.format(alolanDiglett, league)
            assertTrue(summary.contains("· $league\n"))
            assertTrue(summary.contains("diglett_alolan"))
        }
    }
}
