package com.goivlab.iv

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class OpenLeagueMetaTest {
    private val root = File(System.getProperty("goivlab.repoRoot"))
    private val json by lazy { File(root, "data/open-league-pvpoke-rankings.json").readText() }
    private val meta by lazy { OpenLeagueMeta.parse(json) }

    @Test fun ratesPvPPerformanceSeparatelyFromIvSpreadQuality() {
        assertEquals(77.1, meta.get("GL", "venusaur")!!.score, 0.001)
        assertEquals(66.2, meta.get("GL", "ivysaur")!!.score, 0.001)
        assertEquals(52.7, meta.get("GL", "bulbasaur")!!.score, 0.001)
        assertTrue(meta.get("GL", "venusaur")!!.rank < meta.get("GL", "ivysaur")!!.rank)
        // Umbreon remains a distinct evolution branch, not an Eevee IV rank.
        assertEquals(90.2, meta.get("GL", "umbreon")!!.score, 0.001)
    }

    @Test fun missingOpenLeagueRatingIsNotTreatedAsZero() {
        assertNull(meta.get("Fantasy Cup", "venusaur"))
        assertNull(meta.get("GL", "nonexistent_pokemon"))
    }

    @Test fun rejectsUnpinnedOrCorruptMeta() {
        assertFailsWith<IllegalArgumentException> {
            OpenLeagueMeta.parse(json.replace(
                "d98f52174e01f8f67414bc6973599788d098c10e", "different-source"))
        }
    }
}
