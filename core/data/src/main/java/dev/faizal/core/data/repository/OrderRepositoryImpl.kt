package dev.faizal.core.data.repository

import dev.faizal.core.common.pdf.PdfReportGenerator
import dev.faizal.core.data.datasource.dao.OrderDao
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.mapper.toDomain
import dev.faizal.core.data.mapper.toDomainCategoryReport
import dev.faizal.core.data.mapper.toDomainProductReport
import dev.faizal.core.data.mapper.toDomainReport
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.OrderDetail
import dev.faizal.core.domain.model.order.OrderStatus
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.model.report.CategorySalesReport
import dev.faizal.core.domain.model.report.CompleteMonthlyReport
import dev.faizal.core.domain.model.report.DailySalesReport
import dev.faizal.core.domain.model.report.MonthlySalesReport
import dev.faizal.core.domain.model.report.SalesGrowth
import dev.faizal.core.domain.model.report.TopProductReport
import dev.faizal.core.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val pdfGenerator: PdfReportGenerator
) : OrderRepository {

    override suspend fun createOrder(
        orders: List<Order>,
        customerName: String,
        orderStatus: OrderStatus,
        paymentStatus: PaymentStatus
    ): Result<String> {
        return try {
            val orderNumber = generateOrderNumber()
            val timestamp = System.currentTimeMillis()

            val orderEntities = orders.map { order ->
                OrderEntity(
                    orderNumber = orderNumber,
                    menuId = order.menu.id,
                    menuName = order.name,
                    categoryName = order.menu.categoryName,
                    quantity = order.quantity,
                    size = order.size.name,
                    temperature = order.temperature.name,
                    orderType = order.orderType.name,
                    basePrice = order.menu.basePrice,
                    itemPrice = order.totalPrice / order.quantity,
                    totalPrice = order.totalPrice,
                    customerName = customerName,
                    orderDate = timestamp,
                    orderStatus = orderStatus.name,
                    paymentStatus = paymentStatus.name,
                    imageUri = order.imageUri,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            }

            orderDao.insertOrders(orderEntities)
            Result.success(orderNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: Int): OrderDetail? {
        return orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun getOrderByNumber(orderNumber: String): OrderDetail? {
        return orderDao.getOrderByNumber(orderNumber)?.toDomain()
    }

    override fun getAllOrders(): Flow<List<OrderDetail>> {
        return orderDao.getAllOrders().map { it.toDomain() }
    }

    override fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderDetail>> {
        return orderDao.getOrdersByDateRange(startDate, endDate).map { it.toDomain() }
    }

    override fun searchOrders(query: String): Flow<List<OrderDetail>> {
        return orderDao.searchOrders(query).map { it.toDomain() }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: OrderStatus): Result<Unit> {
        return try {
            orderDao.updateOrderStatus(orderId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePaymentStatus(orderId: Int, status: PaymentStatus): Result<Unit> {
        return try {
            orderDao.updatePaymentStatus(orderId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderId: Int): Result<Unit> {
        return try {
            orderDao.deleteOrderById(orderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Statistics ====================

    override fun getDailySalesByMonth(year: Int, month: Int): Flow<List<DailySalesReport>> {
        val yearMonth = formatYearMonth(year, month)
        return orderDao.getDailySalesByMonth(yearMonth).map { it.toDomainReport() }
    }

    override suspend fun getMonthlySales(year: Int, month: Int): MonthlySalesReport? {
        val yearMonth = formatYearMonth(year, month)
        return orderDao.getMonthlySales(yearMonth)?.toDomain()
    }

    override fun getTopProductsByMonth(year: Int, month: Int, limit: Int): Flow<List<TopProductReport>> {
        val yearMonth = formatYearMonth(year, month)
        return orderDao.getTopProductsByMonth(yearMonth, limit).map { it.toDomainProductReport() }
    }

    override fun getCategorySalesByMonth(year: Int, month: Int): Flow<List<CategorySalesReport>> {
        val yearMonth = formatYearMonth(year, month)
        return orderDao.getCategorySalesByMonth(yearMonth).map { it.toDomainCategoryReport() }
    }

    override suspend fun calculateGrowth(year: Int, month: Int): SalesGrowth {
        val currentYearMonth = formatYearMonth(year, month)

        // Calculate previous month
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        calendar.add(Calendar.MONTH, -1)
        val prevYear = calendar.get(Calendar.YEAR)
        val prevMonth = calendar.get(Calendar.MONTH) + 1
        val previousYearMonth = formatYearMonth(prevYear, prevMonth)

        val currentSales = orderDao.getTotalSalesByMonth(currentYearMonth) ?: 0.0
        val previousSales = orderDao.getTotalSalesByMonth(previousYearMonth) ?: 0.0

        val growthAmount = currentSales - previousSales
        val growthPercentage = if (previousSales > 0) {
            (growthAmount / previousSales) * 100
        } else {
            0.0
        }

        return SalesGrowth(
            currentMonthSales = currentSales,
            previousMonthSales = previousSales,
            growthAmount = growthAmount,
            growthPercentage = growthPercentage,
            isPositive = growthAmount >= 0
        )
    }

    override suspend fun getCompleteMonthlyReport(year: Int, month: Int): CompleteMonthlyReport {
        val monthlySales = getMonthlySales(year, month)
        val dailySales = getDailySalesByMonth(year, month).first()
        val topProducts = getTopProductsByMonth(year, month, 10).first()
        val categorySales = getCategorySalesByMonth(year, month).first()
        val growth = calculateGrowth(year, month)

        return CompleteMonthlyReport(
            monthlySales = monthlySales,
            dailySales = dailySales,
            topProducts = topProducts,
            categorySales = categorySales,
            growth = growth
        )
    }

    override fun getOrdersByDate(date: String): Flow<List<OrderDetail>> {
        return orderDao.getOrdersByDate(date).map { it.toDomain() }
    }

    // ==================== PDF Report ====================

    override suspend fun generateMonthlyReportPdf(
        year: Int,
        month: Int,
        outputFile: File
    ): Result<File> {
        return try {
            val report = getCompleteMonthlyReport(year, month)

            pdfGenerator.generateReport(
                outputFile = outputFile,
                year = year,
                month = month,
                report = report
            )

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Helper Functions ====================

    private suspend fun generateOrderNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val lastNumber = orderDao.getLastOrderNumberToday() ?: 0
        val newNumber = lastNumber + 1

        return "ORD-$dateStr-${String.format("%03d", newNumber)}"
    }

    private fun formatYearMonth(year: Int, month: Int): String {
        return String.format("%04d-%02d", year, month)
    }
}