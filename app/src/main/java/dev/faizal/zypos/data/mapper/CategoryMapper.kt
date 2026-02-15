package dev.faizal.zypos.data.mapper

import dev.faizal.zypos.data.datasource.local.entity.CategoryEntity
import dev.faizal.zypos.domain.model.menu.Category

object CategoryMapper {

    fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            emoji = emoji,
            displayOrder = displayOrder,
            isActive = isActive
        )
    }

    fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            emoji = emoji,
            displayOrder = displayOrder,
            isActive = isActive
        )
    }

    fun List<CategoryEntity>.toDomainList(): List<Category> {
        return map { it.toDomain() }
    }
}