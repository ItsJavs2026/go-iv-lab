package com.goivlab.iv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IvCalculatorTest {
    @Test fun floorAndMinimumCp() {
        val result = IvCalculator.calculate(BaseStats(10, 10, 10), IvSpread(0, 0, 0), 0.1)
        assertEquals(10, result.cp)
        assertEquals(10, result.hp)
    }

    @Test fun identicalInputsAreDeterministic() {
        val base = BaseStats(100, 120, 140)
        val iv = IvSpread(0, 15, 15)
        val first = IvCalculator.calculate(base, iv, 0.5)
        assertEquals(first, IvCalculator.calculate(base, iv, 0.5))
        assertEquals(77, first.hp)
        assertTrue(first.statProduct > 0)
    }

    @Test fun higherCpmIncreasesCp() {
        val base = BaseStats(150, 130, 110)
        val iv = IvSpread(10, 10, 10)
        assertTrue(IvCalculator.calculate(base, iv, 0.7).cp >
            IvCalculator.calculate(base, iv, 0.5).cp)
    }

    @Test fun invalidInputsRejected() {
        assertFailsWith<IllegalArgumentException> {
            IvCalculator.calculate(BaseStats(100, 100, 100), IvSpread(16, 0, 0), 0.5)
        }
        assertFailsWith<IllegalArgumentException> {
            IvCalculator.calculate(BaseStats(100, 100, 100), IvSpread(0, 0, 0), Double.NaN)
        }
    }
}
