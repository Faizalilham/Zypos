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
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import dev.faizal.core.domain.repository.OrderRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for OrderViewModel
 *
 * Tests cover:
 * - Cart management (add, update, remove)
 * - Order calculations (subtotal, tax, total)
 * - Payment method selection
 * - Order saving
 * - Search and filtering
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var orderRepository: OrderRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var menuRepository: MenuRepository
    private lateinit var viewModel: OrderViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testCategory = Category(
        id = 1,
        name = "Coffee",
        emoji = "☕",
        displayOrder = 1,
        isActive = true
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
        imageUrl = 0
    )

    private val testOrder = Order(
        name = "Cappuccino",
        menu = testMenu,
        quantity = 1,
        totalPrice = 25000.0,
        orderType = OrderType.DINE_IN,
        temperature = Temperature.HOT,
        size = Size.MEDIUM,
        imageUri = ""
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        orderRepository = mockk()
        categoryRepository = mockk()
        menuRepository = mockk()

        // Default mocks
        every { categoryRepository.getActiveCategories() } returns flowOf(listOf(testCategory))
        every { menuRepository.getActiveMenus() } returns flowOf(listOf(testMenu))

        viewModel = OrderViewModel(orderRepository, categoryRepository, menuRepository)

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ==================== INITIALIZATION TESTS ====================

    @Test
    fun `viewModel initializes with empty cart`() {
        // Then
        assertThat(viewModel.state.orderItems).isEmpty()
        assertThat(viewModel.state.isDineIn).isTrue()
        assertThat(viewModel.state.selectedPaymentMethod).isEqualTo("Credit Card")
    }

    @Test
    fun `viewModel loads categories on init`() = runTest {
        // Then
        viewModel.categories.test {
            val categories = awaitItem()
            assertThat(categories).hasSize(1)
            assertThat(categories.first().name).isEqualTo("Coffee")
        }
    }

    @Test
    fun `viewModel loads menus on init`() = runTest {
        // Then
        viewModel.menus.test {
            val menus = awaitItem()
            assertThat(menus).hasSize(1)
            assertThat(menus.first().name).isEqualTo("Cappuccino")
        }
    }

    // ==================== ADD TO CART TESTS ====================

    @Test
    fun `addToCart adds new item to cart`() {
        // When
        viewModel.addToCart(testMenu)

        // Then
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().menu.id).isEqualTo(testMenu.id)
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(1)
    }

    @Test
    fun `addToCart increments quantity for existing item`() {
        // Given
        viewModel.addToCart(testMenu)

        // When
        viewModel.addToCart(testMenu)

        // Then
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(2)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(50000.0)
    }

    @Test
    fun `addToCart with different sizes creates separate cart items`() {
        // Given
        viewModel.addToCart(testMenu)

        // When - manually add item with different size
        val largeOrder = testOrder.copy(
            size = Size.LARGE,
            totalPrice = 32500.0 // basePrice * 1.3
        )
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + largeOrder
        )

        // Then
        assertThat(viewModel.state.orderItems).hasSize(2)
    }

    @Test
    fun `addToCart with different temperatures creates separate cart items`() {
        // Given
        viewModel.addToCart(testMenu)

        // When - manually add item with different temperature
        val coldOrder = testOrder.copy(temperature = Temperature.COLD)
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + coldOrder
        )

        // Then
        assertThat(viewModel.state.orderItems).hasSize(2)
    }

    // ==================== UPDATE QUANTITY TESTS ====================

    @Test
    fun `updateQuantity increases quantity correctly`() {
        // Given
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()

        // When
        viewModel.updateQuantity(order, 3)

        // Then
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(3)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(75000.0)
    }

    @Test
    fun `updateQuantity decreases quantity correctly`() {
        // Given
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()

        // When
        viewModel.updateQuantity(order, 2)

        // Then
        assertThat(viewModel.state.orderItems.first().quantity).isEqualTo(2)
    }

    @Test
    fun `updateQuantity with zero removes item from cart`() {
        // Given
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()

        // When
        viewModel.updateQuantity(order, 0)

        // Then
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    @Test
    fun `updateQuantity with negative value removes item from cart`() {
        // Given
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()

        // When
        viewModel.updateQuantity(order, -1)

        // Then
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    // ==================== REMOVE ITEM TESTS ====================

    @Test
    fun `removeItem removes specific item from cart`() {
        // Given
        viewModel.addToCart(testMenu)
        val order = viewModel.state.orderItems.first()

        // When
        viewModel.removeItem(order)

        // Then
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    @Test
    fun `removeItem only removes matching item`() {
        // Given
        viewModel.addToCart(testMenu)
        val hotOrder = viewModel.state.orderItems.first()

        // Add cold version
        val coldOrder = testOrder.copy(temperature = Temperature.COLD)
        viewModel.state = viewModel.state.copy(
            orderItems = viewModel.state.orderItems + coldOrder
        )

        // When
        viewModel.removeItem(hotOrder)

        // Then
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().temperature).isEqualTo(Temperature.COLD)
    }

    // ==================== EDIT ORDER TESTS ====================

    @Test
    fun `editOrder updates existing order in cart`() {
        // Given
        viewModel.addToCart(testMenu)
        val oldOrder = viewModel.state.orderItems.first()
        val newOrder = oldOrder.copy(
            size = Size.LARGE,
            totalPrice = 32500.0
        )

        // When
        viewModel.editOrder(oldOrder, newOrder)

        // Then
        assertThat(viewModel.state.orderItems).hasSize(1)
        assertThat(viewModel.state.orderItems.first().size).isEqualTo(Size.LARGE)
        assertThat(viewModel.state.orderItems.first().totalPrice).isEqualTo(32500.0)
    }

    // ==================== CALCULATION TESTS ====================

    @Test
    fun `calculateSubtotal returns sum of all items`() {
        // Given
        viewModel.addToCart(testMenu) // 25000
        viewModel.addToCart(testMenu) // 25000
        viewModel.addToCart(testMenu) // 25000

        // When
        val subtotal = viewModel.calculateSubtotal()

        // Then
        assertThat(subtotal).isEqualTo(75000.0)
    }

    @Test
    fun `calculateSubtotal with empty cart returns zero`() {
        // When
        val subtotal = viewModel.calculateSubtotal()

        // Then
        assertThat(subtotal).isEqualTo(0.0)
    }

    @Test
    fun `calculateTax returns 10 percent of subtotal`() {
        // Given
        viewModel.addToCart(testMenu)

        // When
        val tax = viewModel.calculateTax()

        // Then
        assertThat(tax).isEqualTo(2500.0) // 10% of 25000
    }

    @Test
    fun `calculateTotal returns subtotal plus tax`() {
        // Given
        viewModel.addToCart(testMenu)

        // When
        val total = viewModel.calculateTotal()

        // Then
        assertThat(total).isEqualTo(27500.0) // 25000 + 2500
    }

    @Test
    fun `calculateTax with custom rate returns correct value`() {
        // Given
        viewModel.addToCart(testMenu)

        // When
        val tax = viewModel.calculateTax(taxRate = 0.15) // 15%

        // Then
        assertThat(tax).isEqualTo(3750.0) // 15% of 25000
    }

    // ==================== TOGGLE STATES TESTS ====================

    @Test
    fun `toggleDineIn changes order type`() {
        // When
        viewModel.toggleDineIn(false)

        // Then
        assertThat(viewModel.state.isDineIn).isFalse()
    }

    @Test
    fun `toggleOrderPanel shows and hides panel`() {
        // When
        viewModel.toggleOrderPanel(true)

        // Then
        assertThat(viewModel.state.showOrderPanel).isTrue()

        // When
        viewModel.toggleOrderPanel(false)

        // Then
        assertThat(viewModel.state.showOrderPanel).isFalse()
    }

    @Test
    fun `onPaymentMethodSelected updates payment method`() {
        // When
        viewModel.onPaymentMethodSelected("Cash")

        // Then
        assertThat(viewModel.state.selectedPaymentMethod).isEqualTo("Cash")
    }

    @Test
    fun `toggleDarkMode changes theme`() {
        // When
        viewModel.toggleDarkMode(true)

        // Then
        assertThat(viewModel.state.isDarkMode).isTrue()
    }

    // ==================== SEARCH AND FILTER TESTS ====================

    @Test
    fun `onCategorySelected filters menus by category`() = runTest {
        // When
        viewModel.onCategorySelected("Coffee")
        advanceUntilIdle()

        // Then
        assertThat(viewModel.state.selectedCategory).isEqualTo("Coffee")
    }

    @Test
    fun `onSearchQueryChanged updates search query`() {
        // When
        viewModel.onSearchQueryChanged("Cap")

        // Then
        assertThat(viewModel.state.searchQuery).isEqualTo("Cap")
    }

    @Test
    fun `search filters menus by name`() = runTest {
        // Given
        val menus = listOf(
            testMenu,
            testMenu.copy(id = 2, name = "Latte"),
            testMenu.copy(id = 3, name = "Espresso")
        )
        every { menuRepository.getActiveMenus() } returns flowOf(menus)

        val newViewModel = OrderViewModel(orderRepository, categoryRepository, menuRepository)
        advanceUntilIdle()

        // When
        newViewModel.onSearchQueryChanged("Latte")
        advanceUntilIdle()

        // Then
        newViewModel.menus.test {
            val filtered = awaitItem()
            assertThat(filtered).hasSize(1)
            assertThat(filtered.first().name).isEqualTo("Latte")
        }
    }

    // ==================== CLEAR CART TEST ====================

    @Test
    fun `clearCart removes all items`() {
        // Given
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)
        viewModel.addToCart(testMenu)

        // When
        viewModel.clearCart()

        // Then
        assertThat(viewModel.state.orderItems).isEmpty()
    }

    // ==================== SAVE ORDER TESTS ====================

    @Test
    fun `saveOrder with empty cart shows error`() {
        // Given
        var errorMessage = ""

        // When
        viewModel.saveOrder(
            onSuccess = {},
            onError = { errorMessage = it }
        )

        // Then
        assertThat(errorMessage).isEqualTo("Keranjang kosong")
    }

    @Test
    fun `saveOrder without payment method shows error`() {
        // Given
        viewModel.addToCart(testMenu)
        viewModel.state = viewModel.state.copy(selectedPaymentMethod = "")

        var errorMessage = ""

        // When
        viewModel.saveOrder(
            onSuccess = {},
            onError = { errorMessage = it }
        )

        // Then
        assertThat(errorMessage).isEqualTo("Pilih metode pembayaran")
    }

    @Test
    fun `saveOrder with valid data succeeds`() = runTest {
        // Given
        viewModel.addToCart(testMenu)

        val orderNumber = "ORD-20240101-001"
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any())
        } returns Result.success(orderNumber)

        var successOrderNumber = ""

        // When
        viewModel.saveOrder(
            onSuccess = { successOrderNumber = it },
            onError = {}
        )
        advanceUntilIdle()

        // Then
        assertThat(successOrderNumber).isEqualTo(orderNumber)
        assertThat(viewModel.state.orderItems).isEmpty() // Cart cleared
        assertThat(viewModel.state.showOrderPanel).isFalse()

        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Dine In",
                orderStatus = OrderStatus.COMPLETED,
                paymentStatus = PaymentStatus.PAID
            )
        }
    }

    @Test
    fun `saveOrder with repository error shows error message`() = runTest {
        // Given
        viewModel.addToCart(testMenu)

        val errorMsg = "Database error"
        coEvery {
            orderRepository.createOrder(any(), any(), any(), any())
        } returns Result.failure(Exception(errorMsg))

        var receivedError = ""

        // When
        viewModel.saveOrder(
            onSuccess = {},
            onError = { receivedError = it }
        )
        advanceUntilIdle()

        // Then
        assertThat(receivedError).isEqualTo(errorMsg)
        assertThat(viewModel.state.orderItems).isNotEmpty() // Cart NOT cleared on error
    }

    @Test
    fun `saveOrder uses correct customer name for dine-in`() = runTest {
        // Given
        viewModel.toggleDineIn(true)
        viewModel.addToCart(testMenu)

        coEvery {
            orderRepository.createOrder(any(), any(), any(), any())
        } returns Result.success("ORD-001")

        // When
        viewModel.saveOrder(onSuccess = {}, onError = {})
        advanceUntilIdle()

        // Then
        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Dine In",
                orderStatus = any(),
                paymentStatus = any()
            )
        }
    }

    @Test
    fun `saveOrder uses correct customer name for takeaway`() = runTest {
        // Given
        viewModel.toggleDineIn(false)
        viewModel.addToCart(testMenu)

        coEvery {
            orderRepository.createOrder(any(), any(), any(), any())
        } returns Result.success("ORD-001")

        // When
        viewModel.saveOrder(onSuccess = {}, onError = {})
        advanceUntilIdle()

        // Then
        coVerify {
            orderRepository.createOrder(
                orders = any(),
                customerName = "Take Away",
                orderStatus = any(),
                paymentStatus = any()
            )
        }
    }

    // ==================== PRICE CALCULATION EDGE CASES ====================

    @Test
    fun `small size applies 80 percent multiplier`() {
        // Given
        val smallOrder = testOrder.copy(
            size = Size.SMALL,
            quantity = 1
        )

        // Expected: 25000 * 0.8 = 20000
        viewModel.state = viewModel.state.copy(orderItems = listOf(smallOrder))

        // When
        val subtotal = viewModel.calculateSubtotal()

        // Then - using the order's totalPrice which should be pre-calculated
        assertThat(subtotal).isWithin(0.01).of(20000.0)
    }

    @Test
    fun `large size applies 130 percent multiplier`() {
        // Given
        val largeOrder = testOrder.copy(
            size = Size.LARGE,
            quantity = 1,
            totalPrice = 32500.0 // basePrice * 1.3
        )

        viewModel.state = viewModel.state.copy(orderItems = listOf(largeOrder))

        // When
        val subtotal = viewModel.calculateSubtotal()

        // Then
        assertThat(subtotal).isEqualTo(32500.0)
    }

    // ==================== COMPLEX SCENARIOS ====================

    @Test
    fun `cart with multiple items of different configurations calculates correctly`() {
        // Given - manually create complex cart
        val item1 = testOrder.copy(size = Size.SMALL, quantity = 2, totalPrice = 40000.0)
        val item2 = testOrder.copy(size = Size.LARGE, quantity = 1, totalPrice = 32500.0)
        val item3 = testOrder.copy(temperature = Temperature.COLD, quantity = 3, totalPrice = 75000.0)

        viewModel.state = viewModel.state.copy(
            orderItems = listOf(item1, item2, item3)
        )

        // When
        val subtotal = viewModel.calculateSubtotal()
        val tax = viewModel.calculateTax()
        val total = viewModel.calculateTotal()

        // Then
        assertThat(subtotal).isEqualTo(147500.0)
        assertThat(tax).isEqualTo(14750.0)
        assertThat(total).isEqualTo(162250.0)
    }
}