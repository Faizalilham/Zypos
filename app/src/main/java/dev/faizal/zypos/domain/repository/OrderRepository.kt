// domain/repository/OrderRepository.kt
package dev.faizal.zypos.domain.repository

import dev.faizal.zypos.domain.model.order.Order
import dev.faizal.zypos.domain.model.order.OrderDetail
import dev.faizal.zypos.domain.model.order.OrderStatus
import dev.faizal.zypos.domain.model.order.PaymentStatus
import dev.faizal.zypos.domain.model.report.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface OrderRepository {

    suspend fun createOrder(
        orders: List<Order>,
        customerName: String,
        orderStatus: OrderStatus = OrderStatus.COMPLETED,
        paymentStatus: PaymentStatus = PaymentStatus.PAID
    ): Result<String>

    suspend fun getOrderById(orderId: Int): OrderDetail?

    suspend fun getOrderByNumber(orderNumber: String): OrderDetail?

    fun getAllOrders(): Flow<List<OrderDetail>>

    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderDetail>>

    fun searchOrders(query: String): Flow<List<OrderDetail>>

    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus): Result<Unit>

    suspend fun updatePaymentStatus(orderId: Int, status: PaymentStatus): Result<Unit>

    suspend fun deleteOrder(orderId: Int): Result<Unit>

    fun getDailySalesByMonth(year: Int, month: Int): Flow<List<DailySalesReport>>

    suspend fun getMonthlySales(year: Int, month: Int): MonthlySalesReport?

    fun getTopProductsByMonth(year: Int, month: Int, limit: Int = 3): Flow<List<TopProductReport>>

    fun getCategorySalesByMonth(year: Int, month: Int): Flow<List<CategorySalesReport>>

    suspend fun calculateGrowth(year: Int, month: Int): SalesGrowth

    suspend fun getCompleteMonthlyReport(year: Int, month: Int): CompleteMonthlyReport

    fun getOrdersByDate(date: String): Flow<List<OrderDetail>>

    suspend fun generateMonthlyReportPdf(
        year: Int,
        month: Int,
        outputFile: File
    ): Result<File>
}