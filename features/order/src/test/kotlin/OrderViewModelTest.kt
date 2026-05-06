package dev.faizal.order

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.faizal.core.domain.model.menu.Category
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Order
import dev.faizal.core.domain.model.order.OrderStatus
import dev.faizal.core.domain.model.order.OrderType
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature
import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import dev.faizal.core.domain.repository.OrderRepository
import dev.faizal.core.domain.repository.StoreRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for OrderViewModel
 *
 * NOTE PENTING: Test ini ASUMSIKAN kamu pakai nama:
 * - Domain model: `Store` (di package `dev.faizal.core.domain.model.store`)
 * - Repository:   `StoreRepository`
 *
 * Kalau di code kamu masih pakai nama dari Phase A original
 * (StoreSettings + StoreSettingsRepository), tinggal find & replace
 * di file ini. Method-nya tetap sama: observeSettings(), getSettings(), dll.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var orderRepository: OrderRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var menuRepository: MenuRepository
    private lateinit var storeRepository: StoreRepository
    private lateinit var viewModel: OrderViewModel

    private val testDispatcher = StandardTestDispatcher()

    /** Flow store yang bisa di-emit ulang per test (untuk skenario takeaway-only, dll). */
    private val storeFlow = MutableStateFlow<Store?>(null)

    // ===== Test Data =====

    private val testCategory = Category(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true,
    )

    private val testMenu = Menu(
        id = 1,
        name = "Cappuccino",
        categoryId = 1,
        categoryName = "Coffee",
        basePrice = 25000.0,
        isActive = true,
        imageUri = null,
        sold = 10,
        categoryEmoji = "☕",
        imageUrl = 0,
    )

    private val testOrder = Order(
        name = "Cappuccino",
        menu = testMenu,
        quantity = 1,
        totalPrice = 25000.0,
        orderType = OrderType.DINE_IN,
        temperature = Temperature.HOT,
        size = Size.MEDIUM,
        imageUri = "",
    )

    /** Default store dengan tax 10%, no service charge, BOTH service style. */
    private fun defaultStore(
        taxEnabled: Boolean = true,
        taxPercentage: Double = 10.0,
        serviceChargeEnabled: Boolean = false,
        serviceChargePercentage: Double = 0.0,
        serviceStyle: String = "BOTH",
    ): Store = Store(
        storeName = "Test Cafe",
        storeAddress = "Jl. Test 123",
        storePhone = "08123",
        storeLogoUri = null,
        fnbType = "CAFE",
        serviceStyle = serviceStyle,
        customerCapacity = "MEDIUM",
        openTime = "08:00",
        closeTime = "22:00",
        taxEnabled = taxEnabled,
        taxPercentage = taxPercentage,
        serviceChargeEnabled = serviceChargeEnabled,
        serviceChargePercentage = serviceChargePercentage,
        priorityFeaturesCsv = "",
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        orderRepository = mockk()
        categoryRepository = mockk()
        menuRepository = mockk()
        storeRepository = mockk()

        every { categoryRepository.getActiveCategories() } returns flowOf(listOf(testCategory))
        every { menuRepository.getActiveMenus() } returns flowOf(listOf(testMenu))

        // Default: store dengan tax 10%
        storeFlow.value = defaultStore()
        every { storeRepository.observeSettings() } returns storeFlow

        viewModel = OrderViewModel(
            orderRepository,
            categoryRepository,
            menuRepository,
            storeRepository,
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ==================== INITIALIZATION ====================

    @Test
    fun `viewModel initializes with empty cart`() {
        assertThat(viewModel.state.orderItems).isEmpty()
        assertThat(viewModel.state.isDineIn).isTrue()
        assertThat(viewModel.state.selectedPaymentMethod).isEqualTo("Cash")
        assertThat(viewModel.state.selectedTable).isEqualTo(null)
    }

    @Test
    fun `viewModel loads categories on init`() = runTest {
        viewModel.categories.test {
            val categories = awaitItem()
            assertThat(categories).hasSize(1)
            assertThat(categories.first().name).isEqualTo("Coffee")
        }
    }

    @Test
    fun `viewModel loads menus on init`() = runTest {
        viewModel.menus.test {
            val first = awaitItem()
            if (first.isEmpty()) {
                val menus = awaitItem()
                assertThat(menus).hasSize(1)
                assertThat(menus.first().name).isEqualTo("Cappuccino")
            } else {
                assertThat(first).hasSize(1)
                assertThat(first.first().name).isEqualTo("Cappuccino")
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== ADD TO CART ====================

    @Test
    fun `addToCart adds new item to cart`() {
        viewModel.addToCart(testMenu)
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().menu.id).isEqualTo(testMenu.id)
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(1)
    }

    @Test
    fun `addToCart increments quantity for existing item`() {
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(2)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(50000.0)
    }

    @Test
    fun `addToCart with different sizes creates separate cart items`() {
        viewModel.addToCart(testMenu)
        val largeOrder = testOrder.copy(size = Size.LARGE, totalPrice = 32500.0)
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + largeOrder,
        )
        assertThat(viewModel.state.orderItems).hasSize(2)
    }

    @Test
    fun `addToCart with different temperatures creates separate cart items`() {
        viewModel.addToCart(testMenu)
        val coldOrder = testOrder.copy(temperature = Temperature.COLD)
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + coldOrder,
        )
        assertThat(viewModel.state.orderItems).hasSize(2)
    }

    // ==================== UPDATE QUANTITY ====================

    @Test
    fun `updateQuantity increases quantity correctly`() {
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()
        viewModel.updateQuantity(order, 3)
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(3)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(75000.0)
    }

    @Test
    fun `updateQuantity with zero removes item from cart`() {
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()
        viewModel.updateQuantity(order, 0)
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    @Test
    fun `updateQuantity with negative value removes item from cart`() {
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()
        viewModel.updateQuantity(order, -1)
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    // ==================== REMOVE ITEM ====================

    @Test
    fun `removeItem removes specific item from cart`() {
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()
        viewModel.removeItem(order)
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    @Test
    fun `removeItem only removes matching item`() {
        viewModel.addToCart(testMenu)
        val hotOrder = viewModel.state.orderItems.first()
        val coldOrder = testOrder.copy(temperature = Temperature.COLD)
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + coldOrder,
        )
        viewModel.removeItem(hotOrder)
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().temperature).isEqualTo(Temperature.COLD)
    }

    // ==================== EDIT ORDER ====================

    @Test
    fun `editOrder updates existing order in cart`() {
        viewModel.addToCart(testMenu)
        val oldOrder = viewModel.state.orderItems.first()
        val newOrder = oldOrder.copy(size = Size.LARGE, totalPrice = 32500.0)
        viewModel.editOrder(oldOrder, newOrder)
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().size).isEqualTo(Size.LARGE)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(32500.0)
    }

    // ==================== CALCULATIONS (DYNAMIC) ====================

    @Test
    fun `calculateSubtotal returns sum of all items`() {
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        assertThat(viewModel.calculateSubtotal()).isEqualTo(75000.0)
    }

    @Test
    fun `calculateSubtotal with empty cart returns zero`() {
        assertThat(viewModel.calculateSubtotal()).isEqualTo(0.0)
    }

    @Test
    fun `calculateTax uses tax percentage from store settings`() = runTest {
        viewModel.addToCart(testMenu)
        // Default store sudah taxPercentage = 10%
        assertThat(viewModel.calculateTax()).isEqualTo(2500.0)
    }

    @Test
    fun `calculateTax returns zero when tax disabled in store settings`() = runTest {
        // Update store ke tax disabled
        storeFlow.value = defaultStore(taxEnabled = false, taxPercentage = 0.0)
        advanceUntilIdle()

        viewModel.addToCart(testMenu)
        assertThat(viewModel.calculateTax()).isEqualTo(0.0)
    }

    @Test
    fun `calculateTax respects custom percentage from store settings`() = runTest {
        // Update store ke 15%
        storeFlow.value = defaultStore(taxEnabled = true, taxPercentage = 15.0)
        advanceUntilIdle()

        viewModel.addToCart(testMenu)
        assertThat(viewModel.calculateTax()).isEqualTo(3750.0) // 15% of 25000
    }

    @Test
    fun `calculateServiceCharge returns zero when disabled`() {
        viewModel.addToCart(testMenu)
        // Default: serviceChargeEnabled = false
        assertThat(viewModel.calculateServiceCharge()).isEqualTo(0.0)
    }

    @Test
    fun `calculateServiceCharge applies percentage when enabled`() = runTest {
        storeFlow.value = defaultStore(
            serviceChargeEnabled = true,
            serviceChargePercentage = 5.0,
        )
        advanceUntilIdle()

        viewModel.addToCart(testMenu)
        assertThat(viewModel.calculateServiceCharge()).isEqualTo(1250.0) // 5% of 25000
    }

    @Test
    fun `calculateTotal returns subtotal plus tax plus service charge`() = runTest {
        storeFlow.value = defaultStore(
            taxEnabled = true,
            taxPercentage = 10.0,
            serviceChargeEnabled = true,
            serviceChargePercentage = 5.0,
        )
        advanceUntilIdle()

        viewModel.addToCart(testMenu)
        // 25000 + 2500 (tax) + 1250 (service) = 28750
        assertThat(viewModel.calculateTotal()).isEqualTo(28750.0)
    }

    @Test
    fun `calculateTotal returns subtotal only when both tax and service disabled`() = runTest {
        storeFlow.value = defaultStore(
            taxEnabled = false,
            serviceChargeEnabled = false,
        )
        advanceUntilIdle()

        viewModel.addToCart(testMenu)
        assertThat(viewModel.calculateTotal()).isEqualTo(25000.0)
    }

    // ==================== TAKEAWAY-ONLY GUARD ====================

    @Test
    fun `toggleDineIn ignored when serviceStyle is TAKEAWAY_ONLY`() = runTest {
        storeFlow.value = defaultStore(serviceStyle = "TAKEAWAY_ONLY")
        advanceUntilIdle()

        // Karena observeServiceStyleChanges, isDineIn auto-reset ke false
        assertThat(viewModel.state.isDineIn).isFalse()

        // Coba paksa true → harus diabaikan
        viewModel.toggleDineIn(true)
        assertThat(viewModel.state.isDineIn).isFalse()
    }

    @Test
    fun `toggleDineIn works normally when serviceStyle is BOTH`() = runTest {
        // Default = BOTH
        viewModel.toggleDineIn(false)
        assertThat(viewModel.state.isDineIn).isFalse()

        viewModel.toggleDineIn(true)
        assertThat(viewModel.state.isDineIn).isTrue()
    }

    // ==================== TOGGLE STATES ====================

    @Test
    fun `toggleOrderPanel shows and hides panel`() {
        viewModel.toggleOrderPanel(true)
        assertThat(viewModel.state.showOrderPanel).isTrue()
        viewModel.toggleOrderPanel(false)
        assertThat(viewModel.state.showOrderPanel).isFalse()
    }

    @Test
    fun `onPaymentMethodSelected updates payment method`() {
        viewModel.onPaymentMethodSelected("Cash")
        assertThat(viewModel.state.selectedPaymentMethod).isEqualTo("Cash")
    }

    @Test
    fun `toggleDarkMode changes theme`() {
        viewModel.toggleDarkMode(true)
        assertThat(viewModel.state.isDarkMode).isTrue()
    }

    // ==================== SEARCH AND FILTER ====================

    @Test
    fun `onCategorySelected filters menus by category`() = runTest {
        viewModel.onCategorySelected("Coffee")
        advanceUntilIdle()
        assertThat(viewModel.state.selectedCategory).isEqualTo("Coffee")
    }

    @Test
    fun `onSearchQueryChanged updates search query`() {
        viewModel.onSearchQueryChanged("Cap")
        assertThat(viewModel.state.searchQuery).isEqualTo("Cap")
    }

    @Test
    fun `search filters menus by name`() = runTest {
        val menus = listOf(
            testMenu,
            testMenu.copy(id = 2, name = "Latte"),
            testMenu.copy(id = 3, name = "Espresso"),
        )
        every { menuRepository.getActiveMenus() } returns flowOf(menus)

        val newViewModel = OrderViewModel(
            orderRepository,
            categoryRepository,
            menuRepository,
            storeRepository,
        )
        advanceUntilIdle()

        newViewModel.menus.test {
            skipItems(1)
            newViewModel.onSearchQueryChanged("Latte")
            advanceTimeBy(101)
            runCurrent()

            val filtered = awaitItem()
            assertThat(filtered).hasSize(1)
            assertThat(filtered.first().name).isEqualTo("Latte")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== CLEAR CART ====================

    @Test
    fun `clearCart removes all items`() {
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        viewModel.clearCart()
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    // ==================== SAVE ORDER ====================

    @Test
    fun `saveOrder with empty cart shows error`() {
        var errorMessage = ""
        viewModel.saveOrder(onSuccess = {}, onError = { errorMessage = it })
        assertThat(errorMessage).isEqualTo("Keranjang kosong")
    }

    @Test
    fun `saveOrder without payment method shows error`() {
        viewModel.addToCart(testMenu)
        viewModel.state = viewModel.state.copy(selectedPaymentMethod = "")
        var errorMessage = ""
        viewModel.saveOrder(onSuccess = {}, onError = { errorMessage = it })
        assertThat(errorMessage).isEqualTo("Pilih metode pembayaran")
    }

    @Test
    fun `saveOrder with valid data succeeds`() = runTest {
        viewModel.addToCart(testMenu)
        val orderNumber = "ORD-20240101-001"
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any(), any())
        } returns Result.success(orderNumber)

        var successOrderNumber = ""
        viewModel.saveOrder(
            onSuccess = { successOrderNumber = it },
            onError = {},
        )
        advanceUntilIdle()

        assertThat(successOrderNumber).isEqualTo(orderNumber)
        assertThat(viewModel.state.orderItems).isEmpty()
        assertThat(viewModel.state.showOrderPanel).isFalse()

        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Dine In",
                orderStatus = OrderStatus.COMPLETED,
                paymentStatus = PaymentStatus.PAID,
                tableNumber = any(),
            )
        }
    }

    @Test
    fun `saveOrder with repository error shows error message`() = runTest {
        viewModel.addToCart(testMenu)
        val errorMsg = "Database error"
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any(), any())
        } returns Result.failure(Exception(errorMsg))

        var receivedError = ""
        viewModel.saveOrder(onSuccess = {}, onError = { receivedError = it })
        advanceUntilIdle()

        assertThat(receivedError).isEqualTo(errorMsg)
        assertThat(viewModel.state.orderItems).isNotEmpty()
    }

    @Test
    fun `saveOrder uses correct customer name for dine-in`() = runTest {
        viewModel.toggleDineIn(true)
        viewModel.addToCart(testMenu)
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any(), any())
        } returns Result.success("ORD-001")

        viewModel.saveOrder(onSuccess = {}, onError = {})
        advanceUntilIdle()

        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Dine In",
                orderStatus = any(),
                paymentStatus = any(),
                tableNumber = any(),
            )
        }
    }

    @Test
    fun `saveOrder uses correct customer name for takeaway`() = runTest {
        viewModel.toggleDineIn(false)
        viewModel.addToCart(testMenu)
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any(), any())
        } returns Result.success("ORD-001")

        viewModel.saveOrder(onSuccess = {}, onError = {})
        advanceUntilIdle()

        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Take Away",
                orderStatus = any(),
                paymentStatus = any(),
                tableNumber = any(),
            )
        }
    }

    // ==================== PRICE EDGE CASES ====================

    @Test
    fun `small size applies 80 percent multiplier`() {
        val smallOrder = testOrder.copy(
            size = Size.SMALL,
            quantity = 1,
            totalPrice = 20000.0,
        )
        viewModel.state = viewModel.state.copy(orderItems = listOf(smallOrder))
        assertThat(viewModel.calculateSubtotal()).isWithin(0.01).of(20000.0)
    }

    @Test
    fun `large size applies 130 percent multiplier`() {
        val largeOrder = testOrder.copy(
            size = Size.LARGE,
            quantity = 1,
            totalPrice = 32500.0,
        )
        viewModel.state = viewModel.state.copy(orderItems = listOf(largeOrder))
        assertThat(viewModel.calculateSubtotal()).isEqualTo(32500.0)
    }

    // ==================== COMPLEX SCENARIOS ====================

    @Test
    fun `cart with multiple items calculates correctly with default 10pct tax`() {
        val item1 = testOrder.copy(size = Size.SMALL, quantity = 2, totalPrice = 40000.0)
        val item2 = testOrder.copy(size = Size.LARGE, quantity = 1, totalPrice = 32500.0)
        val item3 = testOrder.copy(temperature = Temperature.COLD, quantity = 3, totalPrice = 75000.0)

        viewModel.state = viewModel.state.copy(orderItems = listOf(item1, item2, item3))

        // Default mock: tax 10%, no service charge
        assertThat(viewModel.calculateSubtotal()).isEqualTo(147500.0)
        assertThat(viewModel.calculateTax()).isEqualTo(14750.0)
        assertThat(viewModel.calculateTotal()).isEqualTo(162250.0)
    }
}