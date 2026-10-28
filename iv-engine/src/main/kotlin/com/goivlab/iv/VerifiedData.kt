package com.goivlab.iv

/**
 * Data is not marked verified merely because it parses.
 * Provenance and independent golden-vector verification are separate release gates.
 */
data class DatasetProvenance(
    val source: String,
    val version: String,
    val retrievedDate: String,
    val license: String,
    val sha256: String
) {
    init {
        require(source.isNotBlank() && version.isNotBlank() && license.isNotBlank())
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(retrievedDate))
        require(Regex("[a-fA-F0-9]{64}").matches(sha256))
    }
}

data class SpeciesForm(
    val speciesId: String,
    val formId: String,
    val base: BaseStats
) {
    init { require(speciesId.isNotBlank() && formId.isNotBlank()) }
}

class RankingDataset(
    val provenance: DatasetProvenance,
    forms: List<SpeciesForm>,
    levels: List<LevelMultiplier>
) {
    val forms: Map<Pair<String, String>, SpeciesForm>
    val levels: List<LevelMultiplier>

    init {
        require(forms.isNotEmpty() && levels.isNotEmpty())
        require(forms.distinctBy { it.speciesId to it.formId }.size == forms.size)
        require(levels.distinctBy { it.level }.size == levels.size)
        val sorted = levels.sortedBy { it.level }
        require(sorted.zipWithNext().all { (a, b) -> a.multiplier < b.multiplier })
        this.forms = forms.associateBy { it.speciesId to it.formId }
        this.levels = sorted
    }

    fun find(speciesId: String, formId: String): SpeciesForm? =
        forms[speciesId to formId]
}
