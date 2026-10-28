package com.goivlab.iv

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PokemonStatsCatalogTest {
    private fun catalog(): PokemonStatsCatalog {
        val root = File(System.getProperty("goivlab.repoRoot"))
        return PokemonStatsCatalog.parse(File(root, "data/pokemon-stats.json").readText())
    }

    @Test fun loadsCheckedInSourceData() {
        val source = catalog()
        assertEquals(1689, source.size)
        assertEquals(BaseStats(108, 81, 67), source.requireSpecies("diglett_alolan").baseStats)
        assertEquals(BaseStats(118, 111, 128), source.requireSpecies("bulbasaur").baseStats)
        assertTrue(source.requireSpecies("diglett_alolan_shadow").name.contains("Shadow"))
        assertEquals(null, source.find("unknown_species"))
    }

    @Test fun lookupMatchesIndependentBulbasaurReference() {
        val lookup = PvpIvLookup(catalog())
        val result = lookup.check("bulbasaur", IvSpread(0, 15, 15))
        assertEquals(770, result.rank)
        assertEquals(4096, result.eligibleSpreads)
        assertEquals(50.0, result.level)
        assertEquals(1118, result.cp)
        assertTrue(result.statProductPercent < 100.0)
    }

    @Test fun lookupMatchesIndependentIvysaurReference() {
        val result = PvpIvLookup(catalog()).check("ivysaur", IvSpread(0, 15, 15))
        assertEquals(39, result.rank)
        assertEquals(37.5, result.level)
        assertEquals(1490, result.cp)
    }

    @Test fun unknownSpeciesMustNotReturnGuessedRank() {
        assertFailsWith<IllegalArgumentException> {
            PvpIvLookup(catalog()).check("unknown_species", IvSpread(0, 15, 15))
        }
    }

    @Test fun duplicateIdsAreRejected() {
        val text = """[
            {"speciesId":"x","name":"X","base":{"attack":100,"defense":100,"stamina":100}},
            {"speciesId":"x","name":"Duplicate","base":{"attack":100,"defense":100,"stamina":100}}
        ]"""
        assertFailsWith<IllegalArgumentException> { PokemonStatsCatalog.parse(text) }
    }
}
