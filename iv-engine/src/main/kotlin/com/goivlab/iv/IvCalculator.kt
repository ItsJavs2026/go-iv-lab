package com.goivlab.iv

import kotlin.math.floor
import kotlin.math.sqrt

data class BaseStats(val attack: Int, val defense: Int, val stamina: Int) {
    init { require(attack > 0 && defense > 0 && stamina > 0) }
}
data class IvSpread(val attack: Int, val defense: Int, val stamina: Int) {
    init { require(attack in 0..15 && defense in 0..15 && stamina in 0..15) }
}
data class CalculatedStats(
    val cp: Int,
    val attack: Double,
    val defense: Double,
    val hp: Int,
    val statProduct: Double
)

object IvCalculator {
    // The caller must supply an authoritative, versioned CP multiplier for the level.
    // This module intentionally does not fabricate a level table.
    fun calculate(base: BaseStats, iv: IvSpread, cpm: Double): CalculatedStats {
        require(cpm > 0.0 && cpm.isFinite())
        val attack = (base.attack + iv.attack) * cpm
        val defense = (base.defense + iv.defense) * cpm
        val stamina = (base.stamina + iv.stamina) * cpm
        val hp = floor(stamina).toInt().coerceAtLeast(10)
        val rawCp = (base.attack + iv.attack) *
            sqrt((base.defense + iv.defense).toDouble()) *
            sqrt((base.stamina + iv.stamina).toDouble()) *
            cpm * cpm / 10.0
        val cp = floor(rawCp).toInt().coerceAtLeast(10)
        return CalculatedStats(cp, attack, defense, hp, attack * defense * hp)
    }
}
