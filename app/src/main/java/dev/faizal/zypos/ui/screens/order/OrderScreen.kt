// ui/screens/order/OrderScreen.kt
package dev.faizal.zypos.ui.screens.order

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.zypos.domain.model.order.Order
import dev.faizal.zypos.ui.screens.order.components.MenuCategories
import dev.faizal.zypos.ui.screens.order.components.MenuItemsGrid
import dev.faizal.zypos.ui.screens.order.components.OrderDetailsPanel
import dev.faizal.zypos.ui.screens.order.components.OrderDetailsPanelContent
import dev.faizal.zypos.ui.utils.ScreenConfig

@Composable
fun OrderScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {}
) {
    val state = viewModel.state

    if (screenConfig.isPhone) {
        // ✅ PHONE MODE
        PhoneOrderScreen(
            viewModel = viewModel,
            state = state,
            onNavigateToRoom = onNavigateToRoom
        )
    } else {
        // ✅ TABLET MODE
        TabletOrderScreen(
            viewModel = viewModel,
            state = state,
            screenConfig = screenConfig,
            onToggleSidebar = onToggleSidebar,
            onNavigateToRoom = onNavigateToRoom
        )
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
            .background(MaterialTheme.colorScheme.background) // ✅ Dynamic background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Order List 🚀",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic text
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ SECTION 1: CATEGORY
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCategories(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ SECTION 2: MENU
            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuItemsGrid(
                menus = menus,
                orderItems = state.orderItems,
                isTabletPortrait = false,
                selectedCategory = state.selectedCategory,
                searchQuery = state.searchQuery,
                onAddToCart = viewModel::addToCart,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (state.orderItems.isNotEmpty()) 80.dp else 0.dp)
            )
        }

        // ✅ Floating Action Button dengan dark mode support
        if (state.orderItems.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.toggleOrderPanel(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary, // ✅ Dynamic
                contentColor = MaterialTheme.colorScheme.onPrimary // ✅ Dynamic
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                Spacer(modifier = Modifier.width(8.dp))
                Text("${state.orderItems.size} Items")
            }
        }

        // ✅ Bottom Sheet dengan dark mode support
        if (state.showOrderPanel) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleOrderPanel(false) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface, // ✅ Dynamic
                contentColor = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ Dynamic background
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
                .padding(bottom = 48.dp)
        ) {
            OrderHeader(
                onMenuClick = onToggleSidebar,
                isTabletPortrait = screenConfig.isTabletPortrait
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ SECTION 1: CATEGORY
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCategories(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ SECTION 2: MENU dengan order button untuk tablet portrait
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
                )

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
                selectedCategory = state.selectedCategory,
                searchQuery = state.searchQuery,
                onAddToCart = viewModel::addToCart,
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
                onMakeOrder = onNavigateToRoom
            )
        }
    }

    Log.d("OrderScreen", "${state.showOrderPanel}")
    if (screenConfig.isTabletPortrait && state.showOrderPanel) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleOrderPanel(false) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface, // ✅ Dynamic
            contentColor = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
private fun OrderHeader(
    onMenuClick: () -> Unit,
    isTabletPortrait: Boolean,
    totalOrdersToday: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isTabletPortrait) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Menu",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order List",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic
                )
                Text(text = " 🚀", fontSize = 24.sp)
            }
            Text(
                text = "total order hari ini: $totalOrdersToday",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ Dynamic
            )
        }
    }
}

@Composable
private fun OrderFloatingButton(
    orderCount: Int,
    onClick: () -> Unit
) {
    Box {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary // ✅ Dynamic
            )
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "View Orders",
                tint = MaterialTheme.colorScheme.onPrimary // ✅ Dynamic
            )
        }
        if (orderCount > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error, // ✅ Dynamic
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
            ) {
                Text(
                    orderCount.toString(),
                    color = MaterialTheme.colorScheme.onError // ✅ Dynamic
                )
            }
        }
    }
}

