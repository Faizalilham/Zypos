package dev.faizal.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val categoryRepository: CategoryRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    var state by mutableStateOf(OrderState())

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _allMenus = MutableStateFlow<List<Menu>>(emptyList())

    val menus: StateFlow<List<Menu>> = combine(
        _allMenus,
        snapshotFlow { state.selectedCategory },
        snapshotFlow { state.searchQuery }
    ) { menuList, selectedCategory, searchQuery ->
        menuList.filter { menu ->
            val matchesCategory = selectedCategory.isNullOrEmpty() ||
                    selectedCategory == "All" ||
                    menu.categoryName == selectedCategory

            val matchesSearch = searchQuery.isEmpty() ||
                    menu.name.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch && menu.isActive
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    init {
        loadCategories()
        loadMenus()
        state = state.copy(selectedCategory = null)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getActiveCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    private fun loadMenus() {
        viewModelScope.launch {
            menuRepository.getActiveMenus().collect { menuList ->
                _allMenus.value = menuList
            }
        }
    }

    private fun <T> snapshotFlow(block: (OrderState) -> T) =
        flow {
            var lastValue = block(state)
            emit(lastValue)

            while (true) {
                delay(100)
                val newValue = block(state)
                if (newValue != lastValue) {
                    lastValue = newValue
                    emit(newValue)
                }
            }
        }

    // ==================== PUBLIC METHODS ====================

    fun addToCart(menu: Menu) {
        val updatedCart = addItemToCart(
            currentItems = state.orderItems,
            menu = menu,
            orderType = if (state.isDineIn) OrderType.DINE_IN else OrderType.TAKE_AWAY
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun updateQuantity(order: Order, newQuantity: Int) {
        val updatedCart = updateItemQuantity(
            currentItems = state.orderItems,
            item = order,
            newQuantity = newQuantity
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun removeItem(order: Order) {
        val updatedCart = removeItemFromCart(
            currentItems = state.orderItems,
            item = order
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun editOrder(oldOrder: Order, newOrder: Order) {
        val updatedCart = state.orderItems.map {
            if (it == oldOrder) newOrder else it
        }
        state = state.copy(orderItems = updatedCart)
    }

    fun toggleDineIn(isDineIn: Boolean) {
        state = state.copy(isDineIn = isDineIn)
    }

    fun toggleOrderPanel(show: Boolean) {
        state = state.copy(showOrderPanel = show)
    }

    fun onPaymentMethodSelected(method: String) {
        state = state.copy(selectedPaymentMethod = method)
    }

    fun toggleDarkMode(isDarkMode: Boolean) {
        state = state.copy(isDarkMode = isDarkMode)
    }

    fun onCategorySelected(category: String) {
        state = state.copy(selectedCategory = category)
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun clearCart() {
        state = state.copy(orderItems = emptyList())
    }

    // ==================== SAVE ORDER ====================

    fun saveOrder(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Validasi
        if (state.orderItems.isEmpty()) {
            onError("Keranjang kosong")
            return
        }

        if (state.selectedPaymentMethod.isEmpty()) {
            onError("Pilih metode pembayaran")
            return
        }

        viewModelScope.launch {
            try {
                val customerName = if (state.isDineIn) "Dine In" else "Take Away"

                val result = orderRepository.createOrder(
                    orders = state.orderItems,
                    customerName = customerName,
                    orderStatus = OrderStatus.COMPLETED,
                    paymentStatus = PaymentStatus.PAID
                )

                result.fold(
                    onSuccess = { orderNumber ->
                        clearCart()
                        state = state.copy(
                            selectedPaymentMethod = "Credit Card",
                            showOrderPanel = false
                        )
                        onSuccess(orderNumber)
                    },
                    onFailure = { exception ->
                        onError(exception.message ?: "Gagal menyimpan")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    // ==================== CART LOGIC ====================

    private fun addItemToCart(
        currentItems: List<Order>,
        menu: Menu,
        name: String = menu.name,
        imageUri : String? = menu.imageUri,
        orderType: OrderType = OrderType.DINE_IN,
        temperature: Temperature = Temperature.HOT,
        size: Size = Size.MEDIUM
    ): List<Order> {
        val existingItem = currentItems.find {
            it.menu.id == menu.id &&
                    it.orderType == orderType &&
                    it.temperature == temperature &&
                    it.size == size
        }

        return if (existingItem != null) {
            currentItems.map { item ->
                if (item.menu.id == menu.id &&
                    item.orderType == orderType &&
                    item.temperature == temperature &&
                    item.size == size
                ) {
                    val newQuantity = item.quantity + 1
                    val itemPrice = calculateItemPrice(menu.basePrice, size)
                    item.copy(
                        quantity = newQuantity,
                        totalPrice = itemPrice * newQuantity
                    )
                } else {
                    item
                }
            }
        } else {
            val itemPrice = calculateItemPrice(menu.basePrice, size)
            currentItems + Order(
                name = name,
                menu = menu,
                quantity = 1,
                totalPrice = itemPrice,
                orderType = orderType,
                temperature = temperature,
                size = size,
                imageUri = imageUri ?: ""
            )
        }
    }

    private fun updateItemQuantity(
        currentItems: List<Order>,
        item: Order,
        newQuantity: Int
    ): List<Order> {
        return if (newQuantity > 0) {
            currentItems.map { currentItem ->
                if (currentItem.menu.id == item.menu.id &&
                    currentItem.orderType == item.orderType &&
                    currentItem.temperature == item.temperature &&
                    currentItem.size == item.size
                ) {
                    val itemPrice = calculateItemPrice(currentItem.menu.basePrice, currentItem.size)
                    currentItem.copy(
                        quantity = newQuantity,
                        totalPrice = itemPrice * newQuantity
                    )
                } else {
                    currentItem
                }
            }
        } else {
            removeItemFromCart(currentItems, item)
        }
    }

    private fun removeItemFromCart(
        currentItems: List<Order>,
        item: Order
    ): List<Order> {
        return currentItems.filter { currentItem ->
            !(currentItem.menu.id == item.menu.id &&
                    currentItem.orderType == item.orderType &&
                    currentItem.temperature == item.temperature &&
                    currentItem.size == item.size)
        }
    }

    private fun calculateItemPrice(basePrice: Double, size: Size): Double {
        return when (size) {
            Size.SMALL -> basePrice * 0.8
            Size.MEDIUM -> basePrice
            Size.LARGE -> basePrice * 1.3
        }
    }

    fun calculateSubtotal(): Double {
        return state.orderItems.sumOf { it.totalPrice }
    }

    fun calculateTax(taxRate: Double = 0.10): Double {
        val subtotal = calculateSubtotal()
        return subtotal * taxRate
    }

    fun calculateTotal(): Double {
        val subtotal = calculateSubtotal()
        val tax = calculateTax()
        return subtotal + tax
    }
}