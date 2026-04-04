package dev.faizal.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.core.designsystem.PrimaryBlue
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Size
import dev.faizal.core.domain.model.order.Temperature
import dev.faizal.order.components.AddOrderDialog
import dev.faizal.order.components.MenuCategories
import dev.faizal.order.components.MenuItemsGrid
import dev.faizal.order.components.OrderDetailsPanel
import dev.faizal.order.components.OrderDetailsPanelContent
import dev.faizal.order.components.OrderFloatingButton
import dev.faizal.order.components.OrderHeader

// Kategori yang langsung addToCart tanpa dialog (no size/temperature)
private val SNACK_CATEGORIES = setOf("Snack", "snack")

private fun Menu.isSnack(): Boolean = categoryName in SNACK_CATEGORIES

@Composable
fun OrderScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {}
) {
    val state = viewModel.state

    if (screenConfig.isPhone) {
        PhoneOrderScreen(viewModel = viewModel, state = state, onNavigateToRoom = onNavigateToRoom)
    } else {
        TabletOrderScreen(
            viewModel = viewModel,
            state = state,
            screenConfig = screenConfig,
            onToggleSidebar = onToggleSidebar,
            onNavigateToRoom = onNavigateToRoom
        )
    }

    // AddOrderDialog — hanya muncul jika pendingMenu bukan snack
    state.pendingMenu?.let { menu ->
        AddOrderDialog(
            menu = menu,
            onDismiss = { viewModel.dismissPendingMenu() },
            onConfirm = { quantity, size, temperature ->
                viewModel.addToCart(
                    menu = menu,
                    quantity = quantity,
                    size = size,
                    temperature = temperature
                )
                viewModel.dismissPendingMenu()
            }
        )
    }
}

/**
 * Dipanggil dari grid saat user klik "+ ADD".
 * - Snack → langsung addToCart (size=null, temperature=null)
 * - Lainnya → buka AddOrderDialog lewat onMenuSelected
 */
private fun handleMenuClick(menu: Menu, viewModel: OrderViewModel) {
    if (menu.isSnack()) {
        viewModel.addToCart(
            menu = menu,
            quantity = 1,
            size = null,
            temperature = null
        )
    } else {
        viewModel.onMenuSelected(menu)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOrderScreen(
    viewModel: OrderViewModel,
    state: OrderState,
    onNavigateToRoom: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val menus by viewModel.menus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order List 🚀",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text("Search...", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.width(200.dp).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))

            MenuCategories(categories = categories, selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Spesial Menu for you", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))

            MenuItemsGrid(
                menus = menus,
                orderItems = state.orderItems,
                isTabletPortrait = false,
                isTabletLandscape = false,
                isPhone = true,
                selectedCategory = state.selectedCategory,
                searchQuery = state.searchQuery,
                onAddToCart = { menu -> handleMenuClick(menu, viewModel) },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (state.orderItems.isNotEmpty()) 72.dp else 0.dp)
            )
        }

        if (state.orderItems.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.toggleOrderPanel(true) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                Spacer(modifier = Modifier.width(8.dp))
                Text("${state.orderItems.size} Items")
            }
        }

        if (state.showOrderPanel) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleOrderPanel(false) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                OrderDetailsPanelContent(
                    isDineIn = state.isDineIn,
                    onDineInChange = viewModel::toggleDineIn,
                    orderItems = state.orderItems,
                    onQuantityChange = viewModel::updateQuantity,
                    onRemoveItem = viewModel::removeItem,
                    onEditItem = viewModel::editOrder,
                    selectedPaymentMethod = state.selectedPaymentMethod,
                    onPaymentMethodChange = viewModel::onPaymentMethodSelected,
                    onMakeOrder = onNavigateToRoom,
                    selectedTable = state.selectedTable,
                    onTableSelected = viewModel::onTableSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletOrderScreen(
    viewModel: OrderViewModel,
    state: OrderState,
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit,
    onNavigateToRoom: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val menus by viewModel.menus.collectAsState()

    // isTabletLandscape = bukan phone, bukan portrait
    val isTabletLandscape = !screenConfig.isPhone && !screenConfig.isTabletPortrait

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderHeader(
                    onMenuClick = onToggleSidebar,
                    isTabletPortrait = screenConfig.isTabletPortrait,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text("Search menu...", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.width(280.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Category", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(12.dp))

            MenuCategories(categories = categories, selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spesial Menu for you", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                if (screenConfig.isTabletPortrait && state.orderItems.isNotEmpty()) {
                    OrderFloatingButton(
                        orderCount = state.orderItems.size,
                        onClick = { viewModel.toggleOrderPanel(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MenuItemsGrid(
                menus = menus,
                orderItems = state.orderItems,
                isTabletPortrait = screenConfig.isTabletPortrait,
                isTabletLandscape = isTabletLandscape,
                selectedCategory = state.selectedCategory,
                searchQuery = state.searchQuery,
                onAddToCart = { menu -> handleMenuClick(menu, viewModel) },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!screenConfig.isTabletPortrait) {
            OrderDetailsPanel(
                isDineIn = state.isDineIn,
                onDineInChange = viewModel::toggleDineIn,
                orderItems = state.orderItems,
                onQuantityChange = viewModel::updateQuantity,
                onRemoveItem = viewModel::removeItem,
                onEditItem = viewModel::editOrder,
                selectedPaymentMethod = state.selectedPaymentMethod,
                onPaymentMethodChange = viewModel::onPaymentMethodSelected,
                onMakeOrder = onNavigateToRoom,
                selectedTable = state.selectedTable,
                onTableSelected = viewModel::onTableSelected,
            )
        }
    }

    if (screenConfig.isTabletPortrait && state.showOrderPanel) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleOrderPanel(false) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            OrderDetailsPanelContent(
                isDineIn = state.isDineIn,
                onDineInChange = viewModel::toggleDineIn,
                orderItems = state.orderItems,
                onQuantityChange = viewModel::updateQuantity,
                onRemoveItem = viewModel::removeItem,
                onEditItem = viewModel::editOrder,
                selectedPaymentMethod = state.selectedPaymentMethod,
                onPaymentMethodChange = viewModel::onPaymentMethodSelected,
                onMakeOrder = onNavigateToRoom,
                selectedTable = state.selectedTable,
                onTableSelected = viewModel::onTableSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}