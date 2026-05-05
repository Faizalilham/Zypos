package dev.faizal.core.data.datasource.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table untuk menyimpan pengaturan toko.
 * Konvensi: id selalu = 1.
 */
@Entity(tableName = "store")
data class StoreEntity(
    @PrimaryKey
    val id: Int = 1,

    val storeName: String,
    val storeAddress: String,
    val storePhone: String,
    val storeLogoUri: String?,

    val fnbType: String,
    val serviceStyle: String,
    val customerCapacity: String,

    val openTime: String,
    val closeTime: String,
    val taxEnabled: Boolean,
    val taxPercentage: Double,
    val serviceChargeEnabled: Boolean,
    val serviceChargePercentage: Double,

    val priorityFeaturesCsv: String,

    val createdAt: Long,
    val updatedAt: Long,
)