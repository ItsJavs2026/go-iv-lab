package com.goivlab.nickname

data class NicknameData(
    val name: String,
    val league: String,
    val rank: Int?,
    val attack: Int,
    val defense: Int,
    val hp: Int
) {
    init {
        require(attack in 0..15 && defense in 0..15 && hp in 0..15)
        require(rank == null || rank > 0)
    }
}

data class NicknameResult(val value: String, val fits: Boolean, val length: Int)

object NicknameFormatter {
    const val MAX_LENGTH = 12

    private val tokens = Regex("\\{(name|league|rank|attack|defense|hp)\\}")

    fun format(template: String, data: NicknameData): NicknameResult {
        val values = mapOf(
            "name" to data.name,
            "league" to data.league,
            "rank" to (data.rank?.toString() ?: "?"),
            "attack" to data.attack.toString(),
            "defense" to data.defense.toString(),
            "hp" to data.hp.toString()
        )
        val result = tokens.replace(template) { match ->
            values.getValue(match.groupValues[1])
        }
        // Count Unicode code points, not UTF-16 code units. Actual in-game
        // acceptance of special symbols must still be tested on device.
        val length = result.codePointCount(0, result.length)
        return NicknameResult(result, length <= MAX_LENGTH, length)
    }
}
