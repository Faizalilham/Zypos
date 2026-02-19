package dev.faizal.order


import dev.faizal.core.domain.model.order.Order

data class OrderState(
    val orderItems: List<Order> = emptyList(),
    val isDineIn: Boolean = true,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val showOrderPanel: Boolean = false,
    val isDarkMode: Boolean = false,
    val totalOrdersToday: Int = 0,
    val selectedPaymentMethod: String = "Credit Card"
)