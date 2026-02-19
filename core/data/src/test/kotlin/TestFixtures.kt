package dev.faizal.core.testing

import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.OrderType
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature

/**
 * Test data fixtures untuk consistency across tests
 */
object TestFixtures {

    // ==================== CATEGORY FIXTURES ====================

    fun categoryEntity(
        id: Int = 1,
        name: String = "Coffee",
        emoji: String = "☕",
        displayOrder: Int = 1,
        isActive: Boolean = true
    ) = CategoryEntity(
        id = id,
        name = name,
        emoji = emoji,
        displayOrder = displayOrder,
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun category(
        id: Int = 1,
        name: String = "Coffee",
        emoji: String = "☕",
        displayOrder: Int = 1,
        isActive: Boolean = true
    ) = Category(
        id = id,
        name = name,
        emoji = emoji,
        displayOrder = displayOrder,
        isActive = isActive
    )

    // ==================== MENU FIXTURES ====================

    fun menuEntity(
        id: Int = 1,
        name: String = "Cappuccino",
        categoryId: Int = 1,
        basePrice: Double = 25000.0,
        isActive: Boolean = true,
        imageUri: String? = null,
        sold: Int = 10
    ) = MenuEntity(
        id = id,
        name = name,
        categoryId = categoryId,
        basePrice = basePrice,
        isActive = isActive,
        imageUri = imageUri,
        sold = sold,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun menu(
        id: Int = 1,
        name: String = "Cappuccino",
        categoryId: Int = 1,
        categoryName: String = "Coffee",
        basePrice: Double = 25000.0,
        isActive: Boolean = true,
        imageUri: String? = null,
        sold: Int = 10
    ) = Menu(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        basePrice = basePrice,
        isActive = isActive,
        imageUri = imageUri,
        sold = sold,
        categoryEmoji = "☕",
        imageUrl = 0
    )

    // ==================== ORDER FIXTURES ====================

    fun orderEntity(
        id: Int = 0,
        orderNumber: String = "ORD-20240101-001",
        menuId: Int = 1,
        menuName: String = "Cappuccino",
        categoryName: String = "Coffee",
        quantity: Int = 1,
        size: String = "MEDIUM",
        temperature: String = "HOT",
        orderType: String = "DINE_IN",
        basePrice: Double = 25000.0,
        itemPrice: Double = 25000.0,
        totalPrice: Double = 25000.0,
        customerName: String = "Dine In",
        orderDate: Long = System.currentTimeMillis(),
        orderStatus: String = "COMPLETED",
        paymentStatus: String = "PAID"
    ) = OrderEntity(
        id = id,
        orderNumber = orderNumber,
        menuId = menuId,
        menuName = menuName,
        categoryName = categoryName,
        quantity = quantity,
        size = size,
        temperature = temperature,
        orderType = orderType,
        basePrice = basePrice,
        itemPrice = itemPrice,
        totalPrice = totalPrice,
        customerName = customerName,
        orderDate = orderDate,
        orderStatus = orderStatus,
        paymentStatus = paymentStatus,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    fun order(
        name: String = "Cappuccino",
        menu: Menu = menu(),
        quantity: Int = 1,
        totalPrice: Double = 25000.0,
        orderType: OrderType = OrderType.DINE_IN,
        temperature: Temperature = Temperature.HOT,
        size: Size = Size.MEDIUM,
        imageUri: String = ""
    ) = Order(
        name = name,
        menu = menu,
        quantity = quantity,
        totalPrice = totalPrice,
        orderType = orderType,
        temperature = temperature,
        size = size,
        imageUri = imageUri
    )

    // ==================== BULK DATA GENERATORS ====================

    fun categoryEntities(count: Int, namePrefix: String = "Category"): List<CategoryEntity> {
        return (1..count).map { i ->
            categoryEntity(
                id = i,
                name = "$namePrefix $i",
                displayOrder = i
            )
        }
    }

    fun menuEntities(count: Int, categoryId: Int = 1, namePrefix: String = "Menu"): List<MenuEntity> {
        return (1..count).map { i ->
            menuEntity(
                id = i,
                name = "$namePrefix $i",
                categoryId = categoryId,
                basePrice = 20000.0 + (i * 5000.0)
            )
        }
    }

    fun orderEntities(count: Int, orderNumberPrefix: String = "ORD"): List<OrderEntity> {
        return (1..count).map { i ->
            orderEntity(
                id = i,
                orderNumber = "$orderNumberPrefix-${String.format("%03d", i)}"
            )
        }
    }
}

/**
 * Extension functions for testing
 */

/**
 * Assert that a Result is successful
 */
fun <T> Result<T>.assertSuccess(): T {
    if (isFailure) {
        throw AssertionError("Expected success but got failure: ${exceptionOrNull()?.message}")
    }
    return getOrThrow()
}

/**
 * Assert that a Result is failure with specific message
 */
fun <T> Result<T>.assertFailure(expectedMessage: String? = null) {
    if (isSuccess) {
        throw AssertionError("Expected failure but got success: $this")
    }

    expectedMessage?.let { expected ->
        val actualMessage = exceptionOrNull()?.message
        if (actualMessage != expected) {
            throw AssertionError("Expected message '$expected' but got '$actualMessage'")
        }
    }
}

/**
 * Create timestamp for specific date
 */
fun timestamp(year: Int, month: Int, day: Int, hour: Int = 10, minute: Int = 0): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month - 1, day, hour, minute, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}