package dev.faizal.zypos.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

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
    val orderNumber: String, // Format: ORD-20240529-001
    val menuId: Int,
    val menuName: String,
    val categoryName: String,
    val quantity: Int,
    val size: String, // SMALL, MEDIUM, LARGE
    val temperature: String, // HOT, COLD
    val orderType: String, // DINE_IN, TAKE_AWAY
    val basePrice: Double,
    val itemPrice: Double, // Price after size calculation
    val totalPrice: Double, // itemPrice * quantity
    val customerName: String,
    val orderDate: Long, // Timestamp in millis
    val orderStatus: String, // PENDING, COMPLETED, CANCELLED
    val paymentStatus: String, // UNPAID, PAID
    val notes: String? = null,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)