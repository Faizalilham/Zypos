package dev.faizal.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.common.pdf.PdfReportGenerator
import dev.faizal.core.data.datasource.dao.OrderDao
import dev.faizal.core.data.datasource.entity.DailySales
import dev.faizal.core.data.datasource.entity.MonthlySales
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.datasource.entity.TopProduct
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.OrderStatus
import dev.faizal.core.domain.model.order.OrderType
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for OrderRepositoryImpl
 *
 * Tests cover:
 * - Order creation with validation
 * - Order retrieval (by ID, number, date range)
 * - Order updates (status, payment)
 * - Order deletion
 * - Statistics calculation (daily, monthly, top products)
 * - PDF report generation
 * - Error handling
 */
class OrderRepositoryImplTest {

    private lateinit var orderDao: OrderDao
    private lateinit var pdfGenerator: PdfReportGenerator
    private lateinit var repository: OrderRepositoryImpl

    // Test data
    private val testMenu = Menu(
        id = 1,
        name = "Cappuccino",
        categoryId = 1,
        categoryName = "Coffee",
        basePrice = 25000.0,
        isActive = true,
        imageUri = null,
        sold = 10,
        imageUrl = 0,
        categoryEmoji = "☕"
    )

    private val testOrder = Order(
        name = "Cappuccino",
        menu = testMenu,
        quantity = 2,
        totalPrice = 50000.0,
        orderType = OrderType.DINE_IN,
        temperature = Temperature.HOT,
        size = Size.MEDIUM,
        imageUri = ""
    )

    private val testOrderEntity = OrderEntity(
        id = 1,
        orderNumber = "ORD-20240101-001",
        menuId = 1,
        menuName = "Cappuccino",
        categoryName = "Coffee",
        quantity = 2,
        size = "MEDIUM",
        temperature = "HOT",
        orderType = "DINE_IN",
        basePrice = 25000.0,
        itemPrice = 25000.0,
        totalPrice = 50000.0,
        customerName = "Dine In",
        orderDate = System.currentTimeMillis(),
        orderStatus = "COMPLETED",
        paymentStatus = "PAID",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        orderDao = mockk()
        pdfGenerator = mockk()
        repository = OrderRepositoryImpl(orderDao, pdfGenerator)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ==================== CREATE ORDER TESTS ====================

    @Test
    fun `createOrder with valid data returns success with order number`() = runTest {
        // Given
        val orders = listOf(testOrder)
        coEvery { orderDao.getLastOrderNumberToday() } returns null
        coEvery { orderDao.insertOrders(any()) } returns listOf(1L)

        // When
        val result = repository.createOrder(
            orders = orders,
            customerName = "Dine In",
            orderStatus = OrderStatus.COMPLETED,
            paymentStatus = PaymentStatus.PAID
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).contains("ORD-")

        coVerify { orderDao.insertOrders(any()) }
    }

    @Test
    fun `createOrder generates sequential order numbers`() = runTest {
        // Given
        val orders = listOf(testOrder)
        coEvery { orderDao.getLastOrderNumberToday() } returns 5
        coEvery { orderDao.insertOrders(any()) } returns listOf(1L)

        // When
        val result = repository.createOrder(
            orders = orders,
            customerName = "Dine In",
            orderStatus = OrderStatus.COMPLETED,
            paymentStatus = PaymentStatus.PAID
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).endsWith("-006") // 5 + 1 = 6
    }

    @Test
    fun `createOrder with database error returns failure`() = runTest {
        // Given
        val orders = listOf(testOrder)
        val errorMessage = "Database error"
        coEvery { orderDao.getLastOrderNumberToday() } returns null
        coEvery { orderDao.insertOrders(any()) } throws Exception(errorMessage)

        // When
        val result = repository.createOrder(
            orders = orders,
            customerName = "Dine In",
            orderStatus = OrderStatus.COMPLETED,
            paymentStatus = PaymentStatus.PAID
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `createOrder calculates correct item prices for different sizes`() = runTest {
        // Given
        val smallOrder = testOrder.copy(size = Size.SMALL, totalPrice = 20000.0)
        val mediumOrder = testOrder.copy(size = Size.MEDIUM, totalPrice = 25000.0)
        val largeOrder = testOrder.copy(size = Size.LARGE, totalPrice = 32500.0)

        val orders = listOf(smallOrder, mediumOrder, largeOrder)

        coEvery { orderDao.getLastOrderNumberToday() } returns null

        val capturedEntities = slot<List<OrderEntity>>()
        coEvery { orderDao.insertOrders(capture(capturedEntities)) } returns listOf(1L, 2L, 3L)

        // When
        repository.createOrder(
            orders = orders,
            customerName = "Test",
            orderStatus = OrderStatus.COMPLETED,
            paymentStatus = PaymentStatus.PAID
        )

        // Then
        val entities = capturedEntities.captured
        assertThat(entities[0].itemPrice).isEqualTo(20000.0) // SMALL: basePrice * 0.8
        assertThat(entities[1].itemPrice).isEqualTo(25000.0) // MEDIUM: basePrice
        assertThat(entities[2].itemPrice).isEqualTo(32500.0) // LARGE: basePrice * 1.3
    }

    // ==================== GET ORDER TESTS ====================

    @Test
    fun `getOrderById with existing order returns order detail`() = runTest {
        // Given
        val orderId = 1
        coEvery { orderDao.getOrderById(orderId) } returns testOrderEntity

        // When
        val result = repository.getOrderById(orderId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.orderNumber).isEqualTo("ORD-20240101-001")
        assertThat(result?.menuName).isEqualTo("Cappuccino")
    }

    @Test
    fun `getOrderById with non-existing order returns null`() = runTest {
        // Given
        val orderId = 999
        coEvery { orderDao.getOrderById(orderId) } returns null

        // When
        val result = repository.getOrderById(orderId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getOrderByNumber returns correct order`() = runTest {
        // Given
        val orderNumber = "ORD-20240101-001"
        coEvery { orderDao.getOrderByNumber(orderNumber) } returns testOrderEntity

        // When
        val result = repository.getOrderByNumber(orderNumber)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.orderNumber).isEqualTo(orderNumber)
    }

    @Test
    fun `getAllOrders returns flow of orders`() = runTest {
        // Given
        val entities = listOf(testOrderEntity, testOrderEntity.copy(id = 2))
        every { orderDao.getAllOrders() } returns flowOf(entities)

        // When & Then
        repository.getAllOrders().test {
            val orders = awaitItem()
            assertThat(orders).hasSize(2)
            awaitComplete()
        }
    }

    @Test
    fun `getOrdersByDateRange filters orders correctly`() = runTest {
        // Given
        val startDate = System.currentTimeMillis() - 86400000 // 1 day ago
        val endDate = System.currentTimeMillis()
        val entities = listOf(testOrderEntity)

        every { orderDao.getOrdersByDateRange(startDate, endDate) } returns flowOf(entities)

        // When & Then
        repository.getOrdersByDateRange(startDate, endDate).test {
            val orders = awaitItem()
            assertThat(orders).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `searchOrders with query returns matching orders`() = runTest {
        // Given
        val query = "Cappuccino"
        val entities = listOf(testOrderEntity)
        every { orderDao.searchOrders(query) } returns flowOf(entities)

        // When & Then
        repository.searchOrders(query).test {
            val orders = awaitItem()
            assertThat(orders).hasSize(1)
            assertThat(orders.first().menuName).contains("Cappuccino")
            awaitComplete()
        }
    }

    // ==================== UPDATE ORDER TESTS ====================

    @Test
    fun `updateOrderStatus with valid status returns success`() = runTest {
        // Given
        val orderId = 1
        val newStatus = OrderStatus.COMPLETED
        coEvery { orderDao.updateOrderStatus(orderId, newStatus.name, any()) } just Runs

        // When
        val result = repository.updateOrderStatus(orderId, newStatus)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { orderDao.updateOrderStatus(orderId, "COMPLETED", any()) }
    }

    @Test
    fun `updateOrderStatus with database error returns failure`() = runTest {
        // Given
        val orderId = 1
        val newStatus = OrderStatus.COMPLETED
        val errorMessage = "Update failed"
        coEvery { orderDao.updateOrderStatus(orderId, any(), any()) } throws Exception(errorMessage)

        // When
        val result = repository.updateOrderStatus(orderId, newStatus)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `updatePaymentStatus changes payment status correctly`() = runTest {
        // Given
        val orderId = 1
        val newStatus = PaymentStatus.PAID
        coEvery { orderDao.updatePaymentStatus(orderId, newStatus.name, any()) } just Runs

        // When
        val result = repository.updatePaymentStatus(orderId, newStatus)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { orderDao.updatePaymentStatus(orderId, "PAID", any()) }
    }

    // ==================== DELETE ORDER TESTS ====================

    @Test
    fun `deleteOrder with valid id returns success`() = runTest {
        // Given
        val orderId = 1
        coEvery { orderDao.deleteOrderById(orderId) } just Runs

        // When
        val result = repository.deleteOrder(orderId)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { orderDao.deleteOrderById(orderId) }
    }

    @Test
    fun `deleteOrder with database error returns failure`() = runTest {
        // Given
        val orderId = 1
        val errorMessage = "Delete failed"
        coEvery { orderDao.deleteOrderById(orderId) } throws Exception(errorMessage)

        // When
        val result = repository.deleteOrder(orderId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    fun `getDailySalesByMonth returns daily sales data`() = runTest {
        // Given
        val year = 2024
        val month = 5
        val dailySales = listOf(
            DailySales(date = "01/05/2024", dayOfMonth = 1, totalAmount = 100000.0, orderCount = 5),
            DailySales(date = "02/05/2024", dayOfMonth = 2, totalAmount = 150000.0, orderCount = 8)
        )
        every { orderDao.getDailySalesByMonth("2024-05") } returns flowOf(dailySales)

        // When & Then
        repository.getDailySalesByMonth(year, month).test {
            val reports = awaitItem()
            assertThat(reports).hasSize(2)
            assertThat(reports[0].totalAmount).isEqualTo(100000.0)
            assertThat(reports[1].orderCount).isEqualTo(8)
            awaitComplete()
        }
    }

    @Test
    fun `getMonthlySales returns monthly aggregated data`() = runTest {
        // Given
        val year = 2024
        val month = 5
        val monthlySales = MonthlySales(
            month = 5,
            year = 2024,
            totalAmount = 5000000.0,
            orderCount = 150,
            productCount = 300,
            customerCount = 100,
            netProfit = 1500000.0
        )
        coEvery { orderDao.getMonthlySales("2024-05") } returns monthlySales

        // When
        val result = repository.getMonthlySales(year, month)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.totalAmount).isEqualTo(5000000.0)
        assertThat(result?.orderCount).isEqualTo(150)
    }

    @Test
    fun `getTopProductsByMonth returns sorted products`() = runTest {
        // Given
        val year = 2024
        val month = 5
        val topProducts = listOf(
            TopProduct("Cappuccino", "Coffee", 50, 1250000.0, null, null),
            TopProduct("Latte", "Coffee", 45, 1125000.0, null, null),
            TopProduct("Espresso", "Coffee", 40, 800000.0, null, null)
        )
        every { orderDao.getTopProductsByMonth("2024-05", 10) } returns flowOf(topProducts)

        // When & Then
        repository.getTopProductsByMonth(year, month, 10).test {
            val products = awaitItem()
            assertThat(products).hasSize(3)
            assertThat(products[0].menuName).isEqualTo("Cappuccino")
            assertThat(products[0].orderCount).isEqualTo(50)
            awaitComplete()
        }
    }

    @Test
    fun `calculateGrowth returns correct growth percentage`() = runTest {
        // Given
        val year = 2024
        val month = 5
        coEvery { orderDao.getTotalSalesByMonth("2024-05") } returns 1000000.0
        coEvery { orderDao.getTotalSalesByMonth("2024-04") } returns 800000.0

        // When
        val growth = repository.calculateGrowth(year, month)

        // Then
        assertThat(growth.currentMonthSales).isEqualTo(1000000.0)
        assertThat(growth.previousMonthSales).isEqualTo(800000.0)
        assertThat(growth.growthAmount).isEqualTo(200000.0)
        assertThat(growth.growthPercentage).isEqualTo(25.0)
        assertThat(growth.isPositive).isTrue()
    }

    @Test
    fun `calculateGrowth with negative growth returns correct values`() = runTest {
        // Given
        val year = 2024
        val month = 5
        coEvery { orderDao.getTotalSalesByMonth("2024-05") } returns 700000.0
        coEvery { orderDao.getTotalSalesByMonth("2024-04") } returns 1000000.0

        // When
        val growth = repository.calculateGrowth(year, month)

        // Then
        assertThat(growth.growthAmount).isEqualTo(-300000.0)
        assertThat(growth.growthPercentage).isEqualTo(-30.0)
        assertThat(growth.isPositive).isFalse()
    }

    @Test
    fun `calculateGrowth with zero previous sales handles division by zero`() = runTest {
        // Given
        val year = 2024
        val month = 5
        coEvery { orderDao.getTotalSalesByMonth("2024-05") } returns 1000000.0
        coEvery { orderDao.getTotalSalesByMonth("2024-04") } returns 0.0

        // When
        val growth = repository.calculateGrowth(year, month)

        // Then
        assertThat(growth.growthPercentage).isEqualTo(0.0)
    }

    // ==================== PDF REPORT TESTS ====================

    @Test
    fun `generateMonthlyReportPdf creates PDF successfully`() = runTest {
        // Given
        val year = 2024
        val month = 5
        val outputFile = mockk<File>(relaxed = true)

        // Mock all repository methods needed for complete report
        coEvery { orderDao.getMonthlySales(any()) } returns MonthlySales(5, 2024, 5000000.0, 150, 300, 100, 1500000.0)
        every { orderDao.getDailySalesByMonth(any()) } returns flowOf(emptyList())
        every { orderDao.getTopProductsByMonth(any(), any()) } returns flowOf(emptyList())
        every { orderDao.getCategorySalesByMonth(any()) } returns flowOf(emptyList())
        coEvery { orderDao.getTotalSalesByMonth("2024-05") } returns 5000000.0
        coEvery { orderDao.getTotalSalesByMonth("2024-04") } returns 4000000.0

        every { pdfGenerator.generateReport(any(), any(), any(), any()) } just Runs

        // When
        val result = repository.generateMonthlyReportPdf(year, month, outputFile)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify { pdfGenerator.generateReport(outputFile, year, month, any()) }
    }

    @Test
    fun `generateMonthlyReportPdf with error returns failure`() = runTest {
        // Given
        val year = 2024
        val month = 5
        val outputFile = mockk<File>(relaxed = true)
        val errorMessage = "PDF generation failed"

        coEvery { orderDao.getMonthlySales(any()) } throws Exception(errorMessage)

        // When
        val result = repository.generateMonthlyReportPdf(year, month, outputFile)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    // ==================== EDGE CASES & VALIDATION ====================

    @Test
    fun `getOrdersByDate with specific date format returns correct orders`() = runTest {
        // Given
        val date = "01/05/2024"
        val entities = listOf(testOrderEntity)
        every { orderDao.getOrdersByDate(date) } returns flowOf(entities)

        // When & Then
        repository.getOrdersByDate(date).test {
            val orders = awaitItem()
            assertThat(orders).hasSize(1)
            awaitComplete()
        }
    }

    @Test
    fun `formatYearMonth formats dates correctly`() = runTest {
        // This tests the private helper function indirectly through getDailySalesByMonth
        val year = 2024
        val month = 5

        every { orderDao.getDailySalesByMonth("2024-05") } returns flowOf(emptyList())

        repository.getDailySalesByMonth(year, month).test {
            awaitItem()
            awaitComplete()
        }

        verify { orderDao.getDailySalesByMonth("2024-05") }
    }
}