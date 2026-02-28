package dev.faizal.zypos.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.core.common.utils.rememberScreenConfig
import dev.faizal.core.designsystem.R
import dev.faizal.dashboard.ReportScreen
import dev.faizal.favorite.FavoriteProductDetailScreen
import dev.faizal.features.menu.MenuManagementScreen
import dev.faizal.order.OrderScreen
import dev.faizal.order.OrderViewModel
import dev.faizal.transaction.TransactionAllScreen
import dev.faizal.ui.component.Sidebar
import dev.faizal.ui.navigation.MainRoute
import dev.faizal.zypos.ui.screens.transaction.TransactionScreen
import kotlinx.coroutines.launch

data class BottomNavItem(
    val iconRes: Int,
    val label: String,
    val route: MainRoute
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(
    orderViewModel: OrderViewModel = hiltViewModel(),
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    LaunchedEffect(isDarkMode) {
        Log.d("DarkMode", "isDarkMode changed to: $isDarkMode")
    }

    val screenConfig = rememberScreenConfig()
    var selectedScreen by remember { mutableStateOf<MainRoute>(MainRoute.Overview) }
    var isSidebarOpen by remember { mutableStateOf(!screenConfig.isPhone && !screenConfig.isTabletPortrait) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (screenConfig.isPhone) {
        PhoneLayout(
            selectedScreen = selectedScreen,
            onScreenSelected = { selectedScreen = it },
            orderViewModel = orderViewModel,
            screenConfig = screenConfig,
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange,
            snackbarHostState = snackbarHostState
        )
    } else {
        TabletLayout(
            selectedScreen = selectedScreen,
            onScreenSelected = { selectedScreen = it },
            isSidebarOpen = isSidebarOpen,
            onToggleSidebar = { isSidebarOpen = !isSidebarOpen },
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange,
            orderViewModel = orderViewModel,
            screenConfig = screenConfig,
            snackbarHostState = snackbarHostState
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PhoneLayout(
    selectedScreen: MainRoute,
    onScreenSelected: (MainRoute) -> Unit,
    orderViewModel: OrderViewModel,
    screenConfig: ScreenConfig,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    val navItems = listOf(
        BottomNavItem(R.drawable.dashboard_outlined, "Dashboard", MainRoute.Overview),
        BottomNavItem(R.drawable.order_list_outlined, "Order", MainRoute.Order),
        BottomNavItem(R.drawable.ic_transaction, "Transaction", MainRoute.TransactionAll),
        BottomNavItem(R.drawable.menu_outlined, "Menu", MainRoute.Menu)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = when (item.route) {
                        is MainRoute.TransactionAll ->
                            selectedScreen is MainRoute.TransactionAll || selectedScreen is MainRoute.TransactionSales
                        else -> selectedScreen::class == item.route::class
                    }

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onScreenSelected(item.route) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = 0.12f),
                                                Color.Transparent,
                                                Color.Transparent,
                                            )
                                        ),
                                        shape = RoundedCornerShape(0.dp)
                                    )
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Indicator bar di atas
                                    Box(
                                        modifier = Modifier
                                            .height(3.dp)
                                            .fillMaxWidth()
                                            .background(
                                                color = primaryColor,
                                                shape = RoundedCornerShape(
                                                    bottomStart = 6.dp,
                                                    bottomEnd = 6.dp
                                                )
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                        tint = primaryColor
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(vertical = 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(13.dp))

                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedScreen) {
                is MainRoute.Overview -> ReportScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = {},
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onNavigationFavorite = { onScreenSelected(MainRoute.TransactionSales) },
                    onNavigationDailySales = { onScreenSelected(MainRoute.TransactionAll) }
                )
                is MainRoute.Order -> OrderScreen(
                    viewModel = orderViewModel,
                    screenConfig = screenConfig,
                    onToggleSidebar = {},
                    onNavigateToRoom = {
                        orderViewModel.saveOrder(
                            onSuccess = { orderNumber ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Order berhasil! #$orderNumber")
                                }
                                onScreenSelected(MainRoute.Overview)
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    }
                )
                is MainRoute.Menu -> MenuManagementScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = {}
                )
                is MainRoute.TransactionAll,
                is MainRoute.TransactionSales -> TransactionScreen(
                    screenConfig = screenConfig,
                    initialTab = if (selectedScreen is MainRoute.TransactionSales) 1 else 0
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TabletLayout(
    selectedScreen: MainRoute,
    onScreenSelected: (MainRoute) -> Unit,
    isSidebarOpen: Boolean,
    onToggleSidebar: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    orderViewModel: OrderViewModel,
    screenConfig: ScreenConfig,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (screenConfig.isTabletPortrait) {
                Sidebar(
                    isCompact = true,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    currentRoute = selectedScreen,
                    onNavigate = onScreenSelected
                )
            } else {
                Sidebar(
                    isCompact = !isSidebarOpen,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    currentRoute = selectedScreen,
                    onNavigate = onScreenSelected
                )
            }

            when (selectedScreen) {
                is MainRoute.Overview -> ReportScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = onToggleSidebar,
                    onNavigationFavorite = { onScreenSelected(MainRoute.TransactionSales) },
                    onNavigationDailySales = { onScreenSelected(MainRoute.TransactionAll) }
                )
                is MainRoute.Order -> OrderScreen(
                    viewModel = orderViewModel,
                    screenConfig = screenConfig,
                    onToggleSidebar = onToggleSidebar,
                    onNavigateToRoom = {
                        orderViewModel.saveOrder(
                            onSuccess = { orderNumber ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Order berhasil! #$orderNumber")
                                }
                                onScreenSelected(MainRoute.Overview)
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    }
                )
                is MainRoute.Menu -> MenuManagementScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = onToggleSidebar
                )
                is MainRoute.TransactionAll -> TransactionAllScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = onToggleSidebar
                )
                is MainRoute.TransactionSales -> FavoriteProductDetailScreen(
                    screenConfig = screenConfig,
                    onToggleSidebar = onToggleSidebar
                )
            }
        }
    }
}