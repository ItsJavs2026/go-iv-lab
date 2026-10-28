package com.goivlab.iv

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Candidate acceptance checks for controls exposed by the Android app.
 * These tests use the actual pinned, checked-in offline species catalog.
 */
class PvpIvLookupLeaguesTest {
    private val catalog by lazy {
        val root = File(System.getProperty("goivlab.repoRoot"))
        PokemonStatsCatalog.parse(File(root, "data/pokemon-stats.json").readText())
    }

    @Test fun littleLeagueRespects500CpAndAllIvCombinations() {
        val result = PvpIvLookup(catalog).check("bulbasaur", IvSpread(0, 15, 15), 500, 50.0)
        assertEquals(4096, result.eligibleSpreads)
        assertTrue(result.cp <= 500)
        assertTrue(result.rank in 1..4096)
    }

    @Test fun ultraLeagueHighCapAllowsPerfectBulbasaurAtLevel50() {
        val result = PvpIvLookup(catalog).check("bulbasaur", IvSpread(15, 15, 15), 2500, 50.0)
        assertEquals(1, result.rank)
        assertEquals(50.0, result.level)
        assertEquals(1260, result.cp)
    }

    @Test fun masterHasNoCpCapAndRanksPerfectIvFirst() {
        val result = PvpIvLookup(catalog).check("bulbasaur", IvSpread(15, 15, 15), Int.MAX_VALUE, 50.0)
        assertEquals(1, result.rank)
        assertEquals(4096, result.eligibleSpreads)
        assertEquals(1260, result.cp)
    }

    @Test fun level51IsExplicitAndDoesNotLeakIntoLevel50Default() {
        val engine = PvpIvLookup(catalog)
        val a = engine.check("bulbasaur", IvSpread(15, 15, 15), Int.MAX_VALUE, 50.0)
        val b = engine.check("bulbasaur", IvSpread(15, 15, 15), Int.MAX_VALUE, 51.0)
        assertEquals(50.0, a.level)
        assertEquals(51.0, b.level)
        assertTrue(b.cp >= a.cp)
    }

    @Test fun regionalAndRegularDiglettAreDistinctBaseStatRecords() {
        val regular = catalog.requireSpecies("diglett")
        val alolan = catalog.requireSpecies("diglett_alolan")
        assertNotEquals(regular.baseStats, alolan.baseStats)
        assertEquals(BaseStats(108, 81, 67), alolan.baseStats)
    }
}
