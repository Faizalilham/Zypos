package dev.faizal.core.data.mapper

import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.domain.model.menu.Category


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