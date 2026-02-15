package dev.faizal.zypos.data.mapper


import dev.faizal.zypos.data.datasource.local.entity.CategoryEntity
import dev.faizal.zypos.data.datasource.local.entity.MenuEntity
import dev.faizal.zypos.domain.model.menu.Menu

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
            imageUrl = android.R.drawable.ic_menu_gallery,
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