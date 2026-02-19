package dev.faizal.core.domain.model.overview

data class StatData(
    val value: String,
    val unit: String,
    val change: String,
    val percentage: String,
    val isPositive: Boolean
)