package com.goivlab.iv

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class EvolutionFamiliesTest {
    private val root = File(System.getProperty("goivlab.repoRoot"))
    private val catalog by lazy {
        PokemonStatsCatalog.parse(File(root, "data/pokemon-stats.json").readText())
    }
    private val families by lazy {
        EvolutionFamilies.parse(File(root, "data/evolution-families.json").readText())
    }

    @Test fun bulbasaurHasThreeExactEvolutionStages() {
        val results = families.stagesFor("bulbasaur", catalog)
        assertEquals(listOf("bulbasaur","ivysaur","venusaur"), results.map { it.species.speciesId })
        assertEquals(listOf("First","Middle","Final"), results.map { it.stage })
        assertEquals(listOf(0, 1, 2), results.map { it.depth })
    }

    @Test fun shadowLineDoesNotSilentlyBecomeRegularForm() {
        val results = families.stagesFor("bulbasaur_shadow", catalog)
        assertEquals(listOf("bulbasaur_shadow","ivysaur_shadow","venusaur_shadow"),
            results.map { it.species.speciesId })
        assertFalse(results.any { !it.species.speciesId.endsWith("_shadow") })
    }

    @Test fun eeveeBranchesRemainAlternativesNotMiddleStages() {
        val results = families.stagesFor("eevee", catalog)
        assertEquals(9, results.size)
        assertEquals("First", results.first().stage)
        assertTrue(results.drop(1).all { it.stage == "Final" })
        assertTrue(results.any { it.species.speciesId == "umbreon" })
        assertTrue(results.any { it.species.speciesId == "sylveon" })
    }

    @Test fun absentOrSingleStageReturnsNoInventedAdvice() {
        assertTrue(families.stagesFor("not_a_real_pokemon", catalog).isEmpty())
        assertTrue(families.stagesFor("mewtwo", catalog).isEmpty())
    }

    @Test fun pinnedFileRejectsOtherSourceAndDuplicateId() {
        val json = File(root, "data/evolution-families.json").readText()
        assertFailsWith<IllegalArgumentException> {
            EvolutionFamilies.parse(json.replace(
                "f5a1c50c9162fac0f0c34bb7a662f995c6506b74", "incorrect-source-sha"))
        }
        assertFailsWith<IllegalArgumentException> {
            EvolutionFamilies.parse("""{
                "schemaVersion":1,
                "sourceGameMasterGitBlobSha1":"f5a1c50c9162fac0f0c34bb7a662f995c6506b74",
                "entries":[{"id":"a"},{"id":"a"}]
            }""")
        }
    }
}
