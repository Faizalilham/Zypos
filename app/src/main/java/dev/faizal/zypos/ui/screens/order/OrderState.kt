package dev.faizal.zypos.ui.screens.order


import dev.faizal.zypos.domain.model.order.Order

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