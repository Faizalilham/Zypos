package dev.faizal.zypos.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

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

private val bottomNavScreens = listOf(
    MainRoute.Overview,
    MainRoute.Order,
    MainRoute.Menu
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
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

    // Screens yang bukan bagian bottom nav → tampilkan full screen tanpa bottom bar
    val isSubScreen = selectedScreen is MainRoute.TransactionAll ||
            selectedScreen is MainRoute.TransactionSales

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // TopAppBar hanya muncul di sub-screen untuk tombol back
            if (isSubScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedScreen) {
                                is MainRoute.TransactionAll -> "All Transactions 💰"
                                is MainRoute.TransactionSales -> "Favorite Products 🏆"
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onScreenSelected(MainRoute.Overview) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            // Bottom nav hanya tampil di screen utama
            if (!isSubScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.dashboard_outlined),
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        selected = selectedScreen is MainRoute.Overview,
                        onClick = { onScreenSelected(MainRoute.Overview) }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.order_list_outlined),
                                contentDescription = "Order"
                            )
                        },
                        label = { Text("Order") },
                        selected = selectedScreen is MainRoute.Order,
                        onClick = { onScreenSelected(MainRoute.Order) }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.menu_outlined),
                                contentDescription = "Menu"
                            )
                        },
                        label = { Text("Menu") },
                        selected = selectedScreen is MainRoute.Menu,
                        onClick = { onScreenSelected(MainRoute.Menu) }
                    )
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
                    // Navigasi ke sub-screen dari tombol di ReportScreen
                    onNavigationFavorite = {
                        onScreenSelected(MainRoute.TransactionSales)
                    },
                    onNavigationDailySales = {
                        onScreenSelected(MainRoute.TransactionAll)
                    }
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
                is MainRoute.TransactionAll -> {
                    TransactionAllScreen(
                        screenConfig = screenConfig,
                        onToggleSidebar = {}
                    )
                }
                is MainRoute.TransactionSales -> {
                    FavoriteProductDetailScreen(
                        screenConfig = screenConfig,
                        onToggleSidebar = {},
                        onBack = { onScreenSelected(MainRoute.Overview) }
                    )
                }
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
                    onNavigationFavorite = {
                        onScreenSelected(MainRoute.TransactionSales)
                    },
                    onNavigationDailySales = {
                        onScreenSelected(MainRoute.TransactionAll)
                    }
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
                is MainRoute.TransactionAll -> {
                    TransactionAllScreen(
                        screenConfig = screenConfig,
                        onToggleSidebar = onToggleSidebar,
                    )
                }
                is MainRoute.TransactionSales -> {
                    FavoriteProductDetailScreen(
                        screenConfig = screenConfig,
                        onToggleSidebar = onToggleSidebar,
                    )
                }
            }
        }
    }
}