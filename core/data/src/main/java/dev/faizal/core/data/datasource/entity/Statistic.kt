package dev.faizal.core.data.datasource.entity

import androidx.room.Embedded
import androidx.room.Relation

// Data class untuk laporan
data class OrderWithDetails(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "menuId", // kolom buat foreign key di OrderEntity
        entityColumn = "id"       // kolom buat primary key di MenuEntity
    )
    val menu: MenuEntity
)

data class DailySales(
    val date: String,
    val dayOfMonth: Int,
    val totalAmount: Double,
    val orderCount: Int
)

data class MonthlySales(
    val month: Int,
    val year: Int,
    val totalAmount: Double,
    val orderCount: Int,
    val productCount: Int,
    val customerCount: Int,
    val netProfit: Double
)

data class CategorySales(
    val categoryName: String,
    val totalAmount: Double,
    val orderCount: Int
)

data class TopProduct(
    val menuName: String,
    val categoryName: String,
    val orderCount: Int,
    val totalAmount: Double,
    val imageUri: String? = null,
    val imageResourceId: Int? = null
)