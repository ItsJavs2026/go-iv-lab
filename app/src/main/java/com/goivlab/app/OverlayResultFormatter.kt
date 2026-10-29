package com.goivlab.app

import com.goivlab.iv.PvpIvLookupResult

/** Immutable, offline rank summary shared by the in-app FLOAT action and overlay. */
internal object OverlayResultFormatter {
    fun format(result: PvpIvLookupResult, leagueCode: String): String {
        require(leagueCode in setOf("LC", "GL", "UL", "ML")) { "Unsupported league" }
        val ivs = result.requestedIvs
        return listOf(
            "${result.species.name} (${result.species.speciesId}) · $leagueCode",
            "IV ${ivs.attack}/${ivs.defense}/${ivs.stamina} · Rank #${result.rank}/${result.eligibleSpreads}",
            "CP ${result.cp} · Lv ${result.level}"
        ).joinToString("\n")
    }
}
