package dev.faizal.zypos.data.datasource.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MenuWithCategory(
    @Embedded val menu: MenuEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity
)