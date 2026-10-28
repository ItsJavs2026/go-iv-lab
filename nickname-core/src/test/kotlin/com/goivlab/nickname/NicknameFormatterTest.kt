package com.goivlab.nickname

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class NicknameFormatterTest {
    private val sample = NicknameData("Diglett", "GL", 42, 13, 14, 11)

    @Test fun compactRank() {
        val result = NicknameFormatter.format("{league}{rank}", sample)
        assertEquals("GL42", result.value)
        assertTrue(result.fits)
    }

    @Test fun ivSpread() {
        assertEquals("13/14/11", NicknameFormatter.format("{attack}/{defense}/{hp}", sample).value)
    }

    @Test fun tooLongIsFlaggedNotTruncated() {
        val result = NicknameFormatter.format("{name}-{league}-{rank}", sample)
        assertFalse(result.fits)
        assertEquals("Diglett-GL-42", result.value)
    }

    @Test fun missingRankIsExplicit() {
        assertEquals("UL?", NicknameFormatter.format("{league}{rank}", sample.copy(league = "UL", rank = null)).value)
    }

    @Test fun rejectsInvalidIvs() {
        assertFailsWith<IllegalArgumentException> { sample.copy(attack = 16) }
    }

    @Test fun countsUnicodeCodePoints() {
        val result = NicknameFormatter.format("★{league}{rank}", sample)
        assertEquals(5, result.length)
    }
}
