package dev.faizal.zypos.domain.model.overview

data class GraphData(
    val dataPoints: List<Pair<String, Float>>,
    val amount: String,
    val growth: String,
    val growthPercentage: String
)