package dev.faizal.core.data.datasource.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = MenuEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("menuId"), Index("orderDate")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderNumber: String,
    val menuId: Int,
    val menuName: String,
    val categoryName: String,
    val quantity: Int,
    val size: String? = null,
    val temperature: String? = null,
    val orderType: String,
    val basePrice: Double,
    val itemPrice: Double,
    val totalPrice: Double,
    val customerName: String,
    val orderDate: Long,
    val orderStatus: String,
    val paymentStatus: String,
    val notes: String? = null,
    val imageUri: String? = null,
    val tableNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)