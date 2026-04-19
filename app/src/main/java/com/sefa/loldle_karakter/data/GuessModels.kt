package com.sefa.loldle_karakter.data

enum class ComparisonState {
    CORRECT,
    PARTIAL,
    WRONG
}

data class AttributeComparison(
    val attributeName: String,
    val value: String,
    val state: ComparisonState
)

data class GuessRow(
    val entityName: String,
    val imageUrl: String,
    val comparisons: List<AttributeComparison>
)