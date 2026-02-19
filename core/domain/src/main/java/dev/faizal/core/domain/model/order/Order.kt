package dev.faizal.core.domain.model.order

import dev.faizal.core.domain.model.menu.Menu


// Untuk cart/checkout (belum disimpan ke DB)
data class Order(
    val name: String,
    val menu: Menu,
    val quantity: Int,
    val imageUri : String,
    val totalPrice: Double,
    val orderType: OrderType,
    val temperature: Temperature,
    val size: Size
)


// Untuk order yang sudah tersimpan di DB
data class OrderDetail(
    val id: Int,
    val orderNumber: String,
    val menuId: Int,
    val menuName: String,
    val categoryName: String,
    val quantity: Int,
    val size: Size,
    val temperature: Temperature,
    val orderType: OrderType,
    val basePrice: Double,
    val itemPrice: Double,
    val totalPrice: Double,
    val customerName: String,
    val orderDate: Long,
    val imageUri: String? = null,
    val orderStatus: OrderStatus,
    val paymentStatus: PaymentStatus
)








