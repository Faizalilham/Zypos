package dev.faizal.core.domain.model.menu

data class Category(
    val id: Int,
    val name: String,
    val emoji: String,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

