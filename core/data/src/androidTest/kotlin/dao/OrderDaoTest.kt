package dao


import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.data.database.AppDatabase
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.dao.OrderDao
import dev.faizal.core.data.datasource.entity.CategoryEntity
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.datasource.entity.OrderEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Integration tests for OrderDao with Room database
 *
 * These tests use an in-memory database to ensure isolation
 * and run actual SQL queries to verify DAO operations
 */
@RunWith(AndroidJUnit4::class)
class OrderDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var orderDao: OrderDao
    private lateinit var menuDao: MenuDao
    private lateinit var categoryDao: CategoryDao

    // Test data
    private lateinit var testCategory: CategoryEntity
    private lateinit var testMenu: MenuEntity

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create in-memory database
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        orderDao = database.orderDao()
        menuDao = database.menuDao()
        categoryDao = database.categoryDao()

        // Insert test category and menu (foreign key requirements)
        testCategory = CategoryEntity(
            id = 1,
            name = "Coffee",
            emoji = "☕",
            displayOrder = 1,
            isActive = true
        )
        categoryDao.insertCategory(testCategory)

        testMenu = MenuEntity(
            id = 1,
            name = "Cappuccino",
            categoryId = 1,
            basePrice = 25000.0,
            isActive = true
        )
        menuDao.insertMenu(testMenu)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== INSERT TESTS ====================

    @Test
    fun insertOrder_insertsDataCorrectly() = runTest {
        // Given
        val order = createTestOrder()

        // When
        val id = orderDao.insertOrder(order)

        // Then
        assertThat(id).isGreaterThan(0)

        val retrieved = orderDao.getOrderById(id.toInt())
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.menuName).isEqualTo("Cappuccino")
    }

    @Test
    fun insertOrders_insertsMultipleOrders() = runTest {
        // Given
        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001"),
            createTestOrder(orderNumber = "ORD-002"),
            createTestOrder(orderNumber = "ORD-003")
        )

        // When
        val ids = orderDao.insertOrders(orders)

        // Then
        assertThat(ids).hasSize(3)
        assertThat(ids.all { it > 0 }).isTrue()
    }

    @Test
    fun insertOrder_withSameOrderNumber_replacesExisting() = runTest {
        // Given
        val order1 = createTestOrder(id = 1, orderNumber = "ORD-001", quantity = 1)
        val order2 = createTestOrder(id = 1, orderNumber = "ORD-001", quantity = 2)

        // When
        orderDao.insertOrder(order1)
        orderDao.insertOrder(order2) // Should replace

        // Then
        val retrieved = orderDao.getOrderById(1)
        assertThat(retrieved?.quantity).isEqualTo(2)
    }

    // ==================== GET TESTS ====================

    @Test
    fun getOrderById_returnsCorrectOrder() = runTest {
        // Given
        val order = createTestOrder()
        val id = orderDao.insertOrder(order)

        // When
        val retrieved = orderDao.getOrderById(id.toInt())

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.orderNumber).isEqualTo(order.orderNumber)
    }

    @Test
    fun getOrderById_withNonExistingId_returnsNull() = runTest {
        // When
        val retrieved = orderDao.getOrderById(999)

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun getOrderByNumber_returnsCorrectOrder() = runTest {
        // Given
        val orderNumber = "ORD-20240101-001"
        val order = createTestOrder(orderNumber = orderNumber)
        orderDao.insertOrder(order)

        // When
        val retrieved = orderDao.getOrderByNumber(orderNumber)

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.menuName).isEqualTo("Cappuccino")
    }

    @Test
    fun getAllOrders_returnsAllOrdersSortedByDateDesc() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", orderDate = now - 200000),
            createTestOrder(orderNumber = "ORD-002", orderDate = now - 100000),
            createTestOrder(orderNumber = "ORD-003", orderDate = now)
        )
        orderDao.insertOrders(orders)

        // When & Then
        orderDao.getAllOrders().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            assertThat(result[0].orderNumber).isEqualTo("ORD-003") // Most recent first
            assertThat(result[2].orderNumber).isEqualTo("ORD-001") // Oldest last
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getOrdersByDateRange_filtersCorrectly() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86400000L
        val twoDaysAgo = now - 172800000L

        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", orderDate = twoDaysAgo),
            createTestOrder(orderNumber = "ORD-002", orderDate = oneDayAgo),
            createTestOrder(orderNumber = "ORD-003", orderDate = now)
        )
        orderDao.insertOrders(orders)

        // When & Then - Get orders from last 36 hours
        val startDate = now - (36 * 3600000L)
        orderDao.getOrdersByDateRange(startDate, now).test {
            val result = awaitItem()
            assertThat(result).hasSize(2) // Should get ORD-002 and ORD-003
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchOrders_byCustomerName() = runTest {
        // Given
        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", customerName = "John Doe"),
            createTestOrder(orderNumber = "ORD-002", customerName = "Jane Smith"),
            createTestOrder(orderNumber = "ORD-003", customerName = "John Carter")
        )
        orderDao.insertOrders(orders)

        // When & Then
        orderDao.searchOrders("John").test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.all { it.customerName.contains("John") }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchOrders_byMenuName() = runTest {
        // Given
        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", menuName = "Cappuccino"),
            createTestOrder(orderNumber = "ORD-002", menuName = "Latte"),
            createTestOrder(orderNumber = "ORD-003", menuName = "Espresso")
        )
        orderDao.insertOrders(orders)

        // When & Then
        orderDao.searchOrders("Latte").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result.first().menuName).isEqualTo("Latte")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== UPDATE TESTS ====================

    @Test
    fun updateOrder_updatesCorrectly() = runTest {
        // Given
        val order = createTestOrder(quantity = 1)
        val id = orderDao.insertOrder(order)

        val updated = order.copy(id = id.toInt(), quantity = 5)

        // When
        orderDao.updateOrder(updated)

        // Then
        val retrieved = orderDao.getOrderById(id.toInt())
        assertThat(retrieved?.quantity).isEqualTo(5)
    }

    @Test
    fun updateOrderStatus_changesStatusCorrectly() = runTest {
        // Given
        val order = createTestOrder(orderStatus = "PENDING")
        val id = orderDao.insertOrder(order)

        // When
        orderDao.updateOrderStatus(id.toInt(), "COMPLETED")

        // Then
        val retrieved = orderDao.getOrderById(id.toInt())
        assertThat(retrieved?.orderStatus).isEqualTo("COMPLETED")
    }

    @Test
    fun updatePaymentStatus_changesStatusCorrectly() = runTest {
        // Given
        val order = createTestOrder(paymentStatus = "UNPAID")
        val id = orderDao.insertOrder(order)

        // When
        orderDao.updatePaymentStatus(id.toInt(), "PAID")

        // Then
        val retrieved = orderDao.getOrderById(id.toInt())
        assertThat(retrieved?.paymentStatus).isEqualTo("PAID")
    }

    // ==================== DELETE TESTS ====================

    @Test
    fun deleteOrderById_removesOrder() = runTest {
        // Given
        val order = createTestOrder()
        val id = orderDao.insertOrder(order)

        // When
        orderDao.deleteOrderById(id.toInt())

        // Then
        val retrieved = orderDao.getOrderById(id.toInt())
        assertThat(retrieved).isNull()
    }

    @Test
    fun deleteAllOrders_removesAllOrders() = runTest {
        // Given
        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001"),
            createTestOrder(orderNumber = "ORD-002"),
            createTestOrder(orderNumber = "ORD-003")
        )
        orderDao.insertOrders(orders)

        // When
        orderDao.deleteAllOrders()

        // Then
        orderDao.getAllOrders().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    fun getDailySalesByMonth_aggregatesCorrectly() = runTest {
        // Given - Create orders for May 2024
        val calendar = Calendar.getInstance()
        calendar.set(2024, 4, 1, 10, 0) // May 1, 2024
        val may1 = calendar.timeInMillis

        calendar.set(2024, 4, 2, 10, 0) // May 2, 2024
        val may2 = calendar.timeInMillis

        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", orderDate = may1, totalPrice = 100000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-002", orderDate = may1, totalPrice = 150000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-003", orderDate = may2, totalPrice = 200000.0, orderStatus = "COMPLETED", paymentStatus = "PAID")
        )
        orderDao.insertOrders(orders)

        // When & Then
        orderDao.getDailySalesByMonth("2024-05").test {
            val result = awaitItem()

            assertThat(result).hasSize(2) // 2 days

            val day1 = result.find { it.dayOfMonth == 1 }
            assertThat(day1?.totalAmount).isEqualTo(250000.0) // 100k + 150k
            assertThat(day1?.orderCount).isEqualTo(2)

            val day2 = result.find { it.dayOfMonth == 2 }
            assertThat(day2?.totalAmount).isEqualTo(200000.0)
            assertThat(day2?.orderCount).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMonthlySales_calculatesCorrectly() = runTest {
        // Given - Create orders for May 2024
        val calendar = Calendar.getInstance()
        calendar.set(2024, 4, 1, 10, 0)
        val may = calendar.timeInMillis

        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", orderDate = may, quantity = 2, totalPrice = 100000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-002", orderDate = may, quantity = 3, totalPrice = 150000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-003", orderDate = may, quantity = 1, totalPrice = 50000.0, orderStatus = "COMPLETED", paymentStatus = "PAID")
        )
        orderDao.insertOrders(orders)

        // When
        val result = orderDao.getMonthlySales("2024-05")

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.totalAmount).isEqualTo(300000.0)
        assertThat(result?.orderCount).isEqualTo(3)
        assertThat(result?.productCount).isEqualTo(6) // 2 + 3 + 1
    }

    @Test
    fun getTopProductsByMonth_sortsCorrectly() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, 4, 1, 10, 0)
        val may = calendar.timeInMillis

        val orders = listOf(
            // Cappuccino - 3 orders
            createTestOrder(orderNumber = "ORD-001", orderDate = may, menuName = "Cappuccino", totalPrice = 25000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-002", orderDate = may, menuName = "Cappuccino", totalPrice = 25000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-003", orderDate = may, menuName = "Cappuccino", totalPrice = 25000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            // Latte - 2 orders
            createTestOrder(orderNumber = "ORD-004", orderDate = may, menuName = "Latte", totalPrice = 30000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-005", orderDate = may, menuName = "Latte", totalPrice = 30000.0, orderStatus = "COMPLETED", paymentStatus = "PAID")
        )
        orderDao.insertOrders(orders)

        // When & Then
        orderDao.getTopProductsByMonth("2024-05", 3).test {
            val result = awaitItem()

            assertThat(result).hasSize(2)
            assertThat(result[0].menuName).isEqualTo("Cappuccino") // Most ordered
            assertThat(result[0].orderCount).isEqualTo(3)
            assertThat(result[1].menuName).isEqualTo("Latte")
            assertThat(result[1].orderCount).isEqualTo(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLastOrderNumberToday_returnsCorrectNumber() = runTest {
        // Given
        val today = System.currentTimeMillis()

        val orders = listOf(
            createTestOrder(orderNumber = "ORD-20240101-001", orderDate = today),
            createTestOrder(orderNumber = "ORD-20240101-002", orderDate = today),
            createTestOrder(orderNumber = "ORD-20240101-005", orderDate = today) // Intentional gap
        )
        orderDao.insertOrders(orders)

        // When
        val lastNumber = orderDao.getLastOrderNumberToday()

        // Then
        assertThat(lastNumber).isEqualTo(5)
    }

    @Test
    fun getTotalSalesByMonth_calculatesCorrectly() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, 4, 1, 10, 0)
        val may = calendar.timeInMillis

        val orders = listOf(
            createTestOrder(orderNumber = "ORD-001", orderDate = may, totalPrice = 100000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-002", orderDate = may, totalPrice = 250000.0, orderStatus = "COMPLETED", paymentStatus = "PAID"),
            createTestOrder(orderNumber = "ORD-003", orderDate = may, totalPrice = 150000.0, orderStatus = "PENDING", paymentStatus = "UNPAID") // Should not count
        )
        orderDao.insertOrders(orders)

        // When
        val total = orderDao.getTotalSalesByMonth("2024-05")

        // Then
        assertThat(total).isEqualTo(350000.0) // Only COMPLETED + PAID
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createTestOrder(
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
    ): OrderEntity {
        return OrderEntity(
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
    }
}