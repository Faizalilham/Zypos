package dev.faizal.core.data.mapper

import dev.faizal.core.data.datasource.entity.StoreEntity
import dev.faizal.core.domain.model.store.Store

object StoreMapper {

    fun StoreEntity.toDomain(): Store = Store(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        storePhone = storePhone,
        storeLogoUri = storeLogoUri,
        fnbType = fnbType,
        serviceStyle = serviceStyle,
        customerCapacity = customerCapacity,
        openTime = openTime,
        closeTime = closeTime,
        taxEnabled = taxEnabled,
        taxPercentage = taxPercentage,
        serviceChargeEnabled = serviceChargeEnabled,
        serviceChargePercentage = serviceChargePercentage,
        priorityFeaturesCsv = priorityFeaturesCsv,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun Store.toEntity(): StoreEntity = StoreEntity(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        storePhone = storePhone,
        storeLogoUri = storeLogoUri,
        fnbType = fnbType,
        serviceStyle = serviceStyle,
        customerCapacity = customerCapacity,
        openTime = openTime,
        closeTime = closeTime,
        taxEnabled = taxEnabled,
        taxPercentage = taxPercentage,
        serviceChargeEnabled = serviceChargeEnabled,
        serviceChargePercentage = serviceChargePercentage,
        priorityFeaturesCsv = priorityFeaturesCsv,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}