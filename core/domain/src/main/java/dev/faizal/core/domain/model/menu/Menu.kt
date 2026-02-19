package dev.faizal.core.domain.model.menu

data class Menu(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val basePrice: Double,
    val isActive: Boolean = true,
    val imageUrl: Int,
    val sold: Int = 0,
    val imageUri: String? = null
)
