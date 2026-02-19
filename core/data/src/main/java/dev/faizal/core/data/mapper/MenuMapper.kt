package dev.faizal.core.data.mapper


import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.domain.model.menu.Menu

object MenuMapper {

    fun MenuEntity.toDomain(category: CategoryEntity): Menu {
        return Menu(
            id = id,
            name = name,
            categoryId = categoryId,
            categoryName = category.name,
            categoryEmoji = category.emoji,
            basePrice = basePrice,
            isActive = isActive,
            imageUrl = 0,
            sold = sold,
            imageUri = imageUri
        )
    }

    fun Menu.toEntity(): MenuEntity {
        return MenuEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            basePrice = basePrice,
            isActive = isActive,
            imageUri = imageUri,
            sold = sold
        )
    }
}