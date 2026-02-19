package dev.faizal.core.data.mapper

import dev.faizal.core.data.datasource.entity.CategorySales
import dev.faizal.core.data.datasource.entity.DailySales
import dev.faizal.core.data.datasource.entity.MonthlySales
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.datasource.entity.TopProduct
import dev.faizal.core.domain.model.order.OrderDetail
import dev.faizal.core.domain.model.order.OrderStatus
import dev.faizal.core.domain.model.order.OrderType
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature
import dev.faizal.core.domain.model.report.CategorySalesReport
import dev.faizal.core.domain.model.report.DailySalesReport
import dev.faizal.core.domain.model.report.MonthlySalesReport
import dev.faizal.core.domain.model.report.TopProductReport

fun OrderEntity.toDomain(): OrderDetail {
    return OrderDetail(
        id = id,
        orderNumber = orderNumber,
        menuId = menuId,
        menuName = menuName,
        categoryName = categoryName,
        quantity = quantity,
        size = Size.valueOf(size),
        temperature = Temperature.valueOf(temperature),
        orderType = OrderType.valueOf(orderType),
        basePrice = basePrice,
        itemPrice = itemPrice,
        totalPrice = totalPrice,
        customerName = customerName,
        orderDate = orderDate,
        imageUri = imageUri,
        orderStatus = OrderStatus.valueOf(orderStatus),
        paymentStatus = PaymentStatus.valueOf(paymentStatus),
    )
}

fun List<OrderEntity>.toDomain(): List<OrderDetail> {
    return map { it.toDomain() }
}

fun OrderDetail.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        orderNumber = orderNumber,
        menuId = menuId,
        menuName = menuName,
        categoryName = categoryName,
        quantity = quantity,
        size = size.name,
        temperature = temperature.name,
        orderType = orderType.name,
        basePrice = basePrice,
        itemPrice = itemPrice,
        totalPrice = totalPrice,
        customerName = customerName,
        orderDate = orderDate,
        orderStatus = orderStatus.name,
        paymentStatus = paymentStatus.name,
    )
}

// ==================== REPORT MAPPERS ====================

fun DailySales.toDomain(): DailySalesReport {
    return DailySalesReport(
        date = date,
        dayOfMonth = dayOfMonth,
        totalAmount = totalAmount,
        orderCount = orderCount
    )
}

fun List<DailySales>.toDomainReport(): List<DailySalesReport> {
    return map { it.toDomain() }
}

fun MonthlySales.toDomain(): MonthlySalesReport {
    return MonthlySalesReport(
        month = month,
        year = year,
        totalAmount = totalAmount,
        orderCount = orderCount,
        productCount = productCount,
        customerCount = customerCount,
        netProfit = netProfit
    )
}

fun CategorySales.toDomain(): CategorySalesReport {
    return CategorySalesReport(
        categoryName = categoryName,
        totalAmount = totalAmount,
        orderCount = orderCount
    )
}

fun List<CategorySales>.toDomainCategoryReport(): List<CategorySalesReport> {
    return map { it.toDomain() }
}

fun TopProduct.toDomain(): TopProductReport {
    return TopProductReport(
        menuName = menuName,
        categoryName = categoryName,
        orderCount = orderCount,
        totalAmount = totalAmount,
        imageUri = imageUri
    )
}

fun List<TopProduct>.toDomainProductReport(): List<TopProductReport> {
    return map { it.toDomain() }
}