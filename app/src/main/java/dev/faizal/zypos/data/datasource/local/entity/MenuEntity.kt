package dev.faizal.zypos.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT // Prevent delete category if used by menu
        )
    ],
    indices = [Index("categoryId")]
)
data class MenuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int, // Foreign key to categories table
    val basePrice: Double,
    val isActive: Boolean = true,
    val imageUri: String? = null,
    val imageResourceId: Int? = null,
    val sold: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)