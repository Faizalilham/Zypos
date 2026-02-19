package dev.faizal.order

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.order.components.MenuCategories
import dev.faizal.order.components.MenuItemsGrid
import dev.faizal.order.components.OrderDetailsPanel
import dev.faizal.order.components.OrderDetailsPanelContent
import dev.faizal.order.components.*

@Composable
fun OrderScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {}
) {
    val state = viewModel.state

    if (screenConfig.isPhone) {
        PhoneOrderScreen(
            viewModel = viewModel,
            state = state,
            onNavigateToRoom = onNavigateToRoom
        )
    } else {
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Order List 🚀",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground 
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCategories(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground 
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

        if (state.orderItems.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.toggleOrderPanel(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary, 
                contentColor = MaterialTheme.colorScheme.onPrimary 
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                Spacer(modifier = Modifier.width(8.dp))
                Text("${state.orderItems.size} Items")
            }
        }


        if (state.showOrderPanel) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
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
            .background(MaterialTheme.colorScheme.background)
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

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground 
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCategories(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground 
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


    if (screenConfig.isTabletPortrait && state.showOrderPanel) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}



