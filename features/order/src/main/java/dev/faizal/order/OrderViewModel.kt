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
import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import dev.faizal.core.domain.repository.OrderRepository
import dev.faizal.core.domain.repository.StoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val categoryRepository: CategoryRepository,
    private val menuRepository: MenuRepository,
    private val storeSettingsRepository: StoreRepository,
) : ViewModel() {

    var state by mutableStateOf(OrderState())

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _allMenus = MutableStateFlow<List<Menu>>(emptyList())

    /**
     * Store settings di-expose sebagai StateFlow agar UI bisa langsung observe.
     * Default null = belum onboarding.
     */
    val storeSettings: StateFlow<Store?> =
        storeSettingsRepository.observeSettings().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val menus: StateFlow<List<Menu>> = combine(
        _allMenus,
        snapshotFlow { state.selectedCategory },
        snapshotFlow { state.searchQuery },
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
        initialValue = emptyList(),
    )

    init {
        loadCategories()
        loadMenus()
        observeServiceStyleChanges()
        state = state.copy(selectedCategory = null)
    }

    /**
     * Auto-set isDineIn = false kalau service style = TAKEAWAY_ONLY.
     * Ini handle juga case user ganti settings di tengah session.
     */
    private fun observeServiceStyleChanges() {
        viewModelScope.launch {
            storeSettings.collectLatest { settings ->
                if (settings?.serviceStyle == "TAKEAWAY_ONLY") {
                    state = state.copy(
                        isDineIn = false,
                        selectedTable = null,
                    )
                }
            }
        }
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

    fun addToCart(
        menu: Menu,
        quantity: Int = 1,
        size: Size? = Size.MEDIUM,
        temperature: Temperature? = Temperature.HOT,
    ) {
        val updatedCart = addItemToCart(
            currentItems = state.orderItems,
            menu = menu,
            quantity = quantity,
            size = size,
            temperature = temperature,
            orderType = if (state.isDineIn) OrderType.DINE_IN else OrderType.TAKE_AWAY,
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun onMenuSelected(menu: Menu) {
        state = state.copy(pendingMenu = menu)
    }

    fun dismissPendingMenu() {
        state = state.copy(pendingMenu = null)
    }

    fun updateQuantity(order: Order, newQuantity: Int) {
        val updatedCart = updateItemQuantity(
            currentItems = state.orderItems,
            item = order,
            newQuantity = newQuantity,
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun removeItem(order: Order) {
        val updatedCart = removeItemFromCart(
            currentItems = state.orderItems,
            item = order,
        )
        state = state.copy(orderItems = updatedCart)
    }

    fun editOrder(oldOrder: Order, newOrder: Order) {
        val updatedCart = state.orderItems.map {
            if (it === oldOrder || it == oldOrder) newOrder else it
        }
        state = state.copy(orderItems = updatedCart)
    }

    fun toggleDineIn(isDineIn: Boolean) {
        // Guard: ignore kalau settings TAKEAWAY_ONLY
        if (storeSettings.value?.serviceStyle == "TAKEAWAY_ONLY" && isDineIn) return
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

    fun onTableSelected(table: String?) {
        state = state.copy(selectedTable = table)
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
        onError: (String) -> Unit,
    ) {
        if (state.orderItems.isEmpty()) { onError("Keranjang kosong"); return }
        if (state.selectedPaymentMethod.isEmpty()) { onError("Pilih metode pembayaran"); return }

        viewModelScope.launch {
            try {
                val customerName = if (state.isDineIn) "Dine In" else "Take Away"
                val result = orderRepository.createOrder(
                    orders = state.orderItems,
                    customerName = customerName,
                    tableNumber = if (state.isDineIn) state.selectedTable else null,
                    orderStatus = OrderStatus.COMPLETED,
                    paymentStatus = PaymentStatus.PAID,
                )
                result.fold(
                    onSuccess = { orderNumber ->
                        clearCart()
                        state = state.copy(selectedPaymentMethod = "Cash", showOrderPanel = false)
                        onSuccess(orderNumber)
                    },
                    onFailure = { exception ->
                        onError(exception.message ?: "Gagal menyimpan")
                    },
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
        quantity: Int = 1,
        orderType: OrderType = OrderType.DINE_IN,
        temperature: Temperature? = Temperature.HOT,
        size: Size? = Size.MEDIUM,
    ): List<Order> {
        val existingIndex = currentItems.indexOfFirst {
            it.menu.id == menu.id &&
                    it.orderType == orderType &&
                    it.temperature == temperature &&
                    it.size == size
        }

        return if (existingIndex >= 0) {
            currentItems.toMutableList().also { list ->
                val existing = list[existingIndex]
                val newQuantity = existing.quantity + quantity
                val itemPrice = calculateItemPrice(menu.basePrice, size)
                list[existingIndex] = existing.copy(
                    quantity = newQuantity,
                    totalPrice = itemPrice * newQuantity,
                )
            }
        } else {
            val itemPrice = calculateItemPrice(menu.basePrice, size)
            currentItems + Order(
                name = menu.name,
                menu = menu,
                quantity = quantity,
                totalPrice = itemPrice * quantity,
                orderType = orderType,
                temperature = temperature,
                size = size,
                imageUri = menu.imageUri ?: "",
            )
        }
    }

    private fun updateItemQuantity(
        currentItems: List<Order>,
        item: Order,
        newQuantity: Int,
    ): List<Order> {
        return if (newQuantity > 0) {
            currentItems.map { currentItem ->
                if (currentItem == item) {
                    val itemPrice = calculateItemPrice(currentItem.menu.basePrice, currentItem.size)
                    currentItem.copy(quantity = newQuantity, totalPrice = itemPrice * newQuantity)
                } else {
                    currentItem
                }
            }
        } else {
            removeItemFromCart(currentItems, item)
        }
    }

    private fun removeItemFromCart(currentItems: List<Order>, item: Order): List<Order> {
        return currentItems.filterIndexed { index, currentItem ->
            !(currentItem == item && index == currentItems.indexOf(item))
        }
    }

    private fun calculateItemPrice(basePrice: Double, size: Size?): Double {
        return when (size) {
            Size.SMALL -> basePrice * 0.8
            Size.LARGE -> basePrice * 1.3
            else -> basePrice
        }
    }

    fun calculateSubtotal(): Double = state.orderItems.sumOf { it.totalPrice }

    /**
     * DYNAMIC tax — pakai persentase dari settings.
     * Return 0 kalau tax tidak aktif atau settings null.
     */
    fun calculateTax(): Double {
        val settings = storeSettings.value ?: return 0.0
        if (!settings.taxEnabled) return 0.0
        return calculateSubtotal() * (settings.taxPercentage / 100.0)
    }

    /**
     * DYNAMIC service charge.
     */
    fun calculateServiceCharge(): Double {
        val settings = storeSettings.value ?: return 0.0
        if (!settings.serviceChargeEnabled) return 0.0
        return calculateSubtotal() * (settings.serviceChargePercentage / 100.0)
    }

    fun calculateTotal(): Double = calculateSubtotal() + calculateTax() + calculateServiceCharge()
}