package com.goivlab.iv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VerifiedDataTest {
    private val provenance = DatasetProvenance(
        source = "synthetic-test-fixture",
        version = "test-1",
        retrievedDate = "2026-10-28",
        license = "test-only",
        sha256 = "0".repeat(64)
    )

    @Test fun formsAreKeyedBySpeciesAndForm() {
        val dataset = RankingDataset(
            provenance,
            listOf(
                SpeciesForm("testmon", "normal", BaseStats(100, 100, 100)),
                SpeciesForm("testmon", "alternate", BaseStats(110, 90, 100))
            ),
            listOf(LevelMultiplier(1.0, 0.1), LevelMultiplier(2.0, 0.2))
        )
        assertEquals(110, dataset.find("testmon", "alternate")?.base?.attack)
        assertEquals(null, dataset.find("testmon", "unknown"))
    }

    @Test fun duplicateFormsAreRejected() {
        val form = SpeciesForm("testmon", "normal", BaseStats(100, 100, 100))
        assertFailsWith<IllegalArgumentException> {
            RankingDataset(provenance, listOf(form, form), listOf(LevelMultiplier(1.0, 0.1)))
        }
    }

    @Test fun invalidChecksumIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            provenance.copy(sha256 = "unknown")
        }
    }
}
