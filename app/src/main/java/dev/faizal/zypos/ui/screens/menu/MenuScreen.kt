package dev.faizal.zypos.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.zypos.R
import dev.faizal.zypos.domain.model.menu.Menu
import dev.faizal.zypos.ui.common.Header
import dev.faizal.zypos.ui.screens.menu.components.*
import dev.faizal.zypos.ui.screens.order.OrderViewModel
import dev.faizal.zypos.ui.utils.ScreenConfig

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MenuManagementScreen(
    orderViewModel: OrderViewModel,
    screenConfig: ScreenConfig,
    onToggleSidebar: () -> Unit = {}
) {
    val state = orderViewModel.state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Category", "Menu")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ Dynamic background
    ) {
        // ✅ Header - Responsive
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background) // ✅ Dynamic
                .padding(
                    horizontal = if (screenConfig.isPhone) 16.dp else 24.dp,
                    vertical = 16.dp
                )
        ) {
            if (screenConfig.isPhone) {
                // ✅ PHONE: Simple header
                Text(
                    text = "Menu Management 🍽️",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground // ✅ Dynamic text color
                )
            } else {
                // ✅ TABLET: Full header
                Header(
                    title = "Menu Management",
                    searchQuery = "",
                    onSearchChange = { },
                    onMenuClick = onToggleSidebar,
                    isTabletPortrait = screenConfig.isTabletPortrait
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (screenConfig.isPhone) 16.dp else 24.dp)
        ) {
            // ✅ Tab Selector - Dark Mode Support
            Surface(
                modifier = Modifier.width(if (screenConfig.isPhone) 200.dp else 300.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface, // ✅ Dynamic surface
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (screenConfig.isPhone) 44.dp else 48.dp)
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                ) // ✅ Dynamic
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = if (screenConfig.isPhone) 13.sp else 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary // ✅ Dynamic
                            )
                        }

                        if (index < tabs.size - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (screenConfig.isPhone) 16.dp else 24.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState > initialState) 300 else -300 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) with slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -300 else 300 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        0 -> CategoryTab(isPhone = screenConfig.isPhone)
                        1 -> MenuTab(isPhone = screenConfig.isPhone)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTab(
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    isPhone: Boolean = false
) {
    val categoryState = categoryViewModel.state
    val categoriesWithCount by categoryViewModel.categories.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryWithCount?>(null) }

    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 10

    val totalPages = (categoriesWithCount.size + itemsPerPage - 1) / itemsPerPage
    val startIndex = (currentPage - 1) * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, categoriesWithCount.size)
    val currentPageItems = categoriesWithCount.drop(startIndex).take(itemsPerPage)

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // ✅ Dynamic
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories (${categoriesWithCount.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // ✅ Dynamic
                )

                Button(
                    onClick = { categoryViewModel.toggleCreateDialog(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    if (!isPhone) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Category")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoriesWithCount.isEmpty()) {
                EmptyCategoryState()
            } else {
                // Table Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant, // ✅ Dynamic
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "No",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
                            modifier = Modifier.width(50.dp)
                        )
                        Text(
                            "Emoji",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            "Category Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(100.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Actions",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(120.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentPageItems) { item ->
                        val actualIndex = categoriesWithCount.indexOf(item) + 1
                        CategoryTableRow(
                            item = item,
                            actualIndex = actualIndex,
                            onEdit = {
                                categoryViewModel.toggleEditDialog(true, item)
                            },
                            onDelete = {
                                categoryToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (totalPages > 1) {
                    PaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageChange = { currentPage = it }
                    )
                }
            }
        }
    }

    // Dialogs (tetap sama)
    if (categoryState.showCreateDialog) {
        AddCategoryDialog(
            categoryState = categoryState,
            onDismiss = { categoryViewModel.toggleCreateDialog(false) },
            onNameChange = categoryViewModel::onNameChange,
            onEmojiChange = categoryViewModel::onEmojiChange,
            onDisplayOrderChange = categoryViewModel::onDisplayOrderChange,
            onIsActiveChange = categoryViewModel::onIsActiveChange,
            onCreate = categoryViewModel::createCategory
        )
    }

    if (categoryState.showEditDialog) {
        EditCategoryDialog(
            categoryState = categoryState,
            onDismiss = { categoryViewModel.toggleEditDialog(false) },
            onNameChange = categoryViewModel::onNameChange,
            onEmojiChange = categoryViewModel::onEmojiChange,
            onDisplayOrderChange = categoryViewModel::onDisplayOrderChange,
            onIsActiveChange = categoryViewModel::onIsActiveChange,
            onUpdate = categoryViewModel::updateCategory
        )
    }

    if (showDeleteDialog && categoryToDelete != null) {
        DeleteCategoryDialog(
            categoryName = categoryToDelete!!.category.name,
            onDismiss = {
                showDeleteDialog = false
                categoryToDelete = null
            },
            onConfirm = {
                categoryViewModel.deleteCategory(categoryToDelete!!.category.id)
                showDeleteDialog = false
                categoryToDelete = null
            }
        )
    }
}

@Composable
fun MenuTab(
    menuViewModel: MenuViewModel = hiltViewModel(),
    isPhone: Boolean = false
) {
    val menuState = menuViewModel.state
    val menus by menuViewModel.filteredMenus.collectAsState()
    val categories by menuViewModel.categories.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<Menu?>(null) }

    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 10

    val totalPages = (menus.size + itemsPerPage - 1) / itemsPerPage
    val startIndex = (currentPage - 1) * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, menus.size)
    val currentPageItems = menus.drop(startIndex).take(itemsPerPage)

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // ✅ Dynamic
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu Items (${menus.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // ✅ Dynamic
                )

                Button(
                    onClick = { menuViewModel.toggleCreateDialog(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    if (!isPhone) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Menu")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (menus.isEmpty()) {
                EmptyMenuState()
            } else {
                // Table Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant, // ✅ Dynamic
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isLandscape = !isPhone

                        Text("No", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
                            modifier = Modifier.width(if (isLandscape) 35.dp else 40.dp))

                        Text("Image", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(if (isLandscape) 60.dp else 70.dp))

                        Text("Menu Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.35f))

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.2f),
                            textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Price", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.25f),
                            textAlign = TextAlign.End)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.15f),
                            textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Actions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(if (isLandscape) 80.dp else 100.dp),
                            textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentPageItems) { item ->
                        val actualIndex = menus.indexOf(item) + 1
                        MenuTableRow(
                            item = item,
                            actualIndex = actualIndex,
                            onEdit = {
                                menuViewModel.toggleEditDialog(true, item)
                            },
                            onDelete = {
                                menuToDelete = item
                                showDeleteDialog = true
                            },
                            isLandscape = !isPhone
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (totalPages > 1) {
                    PaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageChange = { currentPage = it }
                    )
                }
            }
        }
    }

    // Dialogs (tetap sama)
    if (menuState.showCreateDialog) {
        AddMenuDialog(
            menuState = menuState,
            categories = categories,
            onDismiss = { menuViewModel.toggleCreateDialog(false) },
            onNameChange = menuViewModel::onNameChange,
            onCategoryChange = menuViewModel::onCategoryChange,
            onPriceChange = menuViewModel::onPriceChange,
            onImageSelected = menuViewModel::onImageSelected,
            onIsActiveChange = menuViewModel::onIsActiveChange,
            onCreate = menuViewModel::createMenu
        )
    }

    if (menuState.showEditDialog) {
        EditMenuDialog(
            menuState = menuState,
            categories = categories,
            onDismiss = { menuViewModel.toggleEditDialog(false) },
            onNameChange = menuViewModel::onNameChange,
            onCategoryChange = menuViewModel::onCategoryChange,
            onPriceChange = menuViewModel::onPriceChange,
            onImageSelected = menuViewModel::onImageSelected,
            onIsActiveChange = menuViewModel::onIsActiveChange,
            onUpdate = menuViewModel::updateMenu
        )
    }

    if (showDeleteDialog && menuToDelete != null) {
        DeleteMenuDialog(
            menuName = menuToDelete!!.name,
            onDismiss = {
                showDeleteDialog = false
                menuToDelete = null
            },
            onConfirm = {
                menuViewModel.deleteMenu(menuToDelete!!.id)
                showDeleteDialog = false
                menuToDelete = null
            }
        )
    }
}

// ==================== TABLE ROWS ====================

@Composable
fun MenuTableRow(
    item: Menu,
    actualIndex: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLandscape: Boolean = false
) {
    val categoryColor = getCategoryColor(item.categoryName)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface, // ✅ Dynamic
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // No
            Text(
                text = "$actualIndex",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
                modifier = Modifier.width(if (isLandscape) 35.dp else 40.dp)
            )

            // Menu Image
            Box(
                modifier = Modifier
                    .width(if (isLandscape) 60.dp else 70.dp)
                    .height(if (isLandscape) 45.dp else 50.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (!item.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Menu Image",
                        modifier = Modifier
                            .size(if (isLandscape) 45.dp else 50.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 45.dp else 50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant), // ✅ Dynamic
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Menu Name
            Text(
                text = item.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
                modifier = Modifier.weight(0.35f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Category Badge
            Box(
                modifier = Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.categoryName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price
            Text(
                text = "Rp ${String.format("%,d", item.basePrice.toInt()).replace(",", ".")}",
                fontSize = if (isLandscape) 13.sp else 14.sp,
                color = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.25f),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Status Badge
            Box(
                modifier = Modifier.weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (item.isActive) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (item.isActive) "Active" else "Inactive",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            Row(
                modifier = Modifier.width(if (isLandscape) 80.dp else 100.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTableRow(
    item: CategoryWithCount,
    actualIndex: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface, // ✅ Dynamic
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$actualIndex",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
                modifier = Modifier.width(50.dp)
            )
            Text(item.category.emoji, fontSize = 24.sp, modifier = Modifier.width(80.dp))
            Text(
                item.category.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, // ✅ Dynamic
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (item.category.isActive) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color(0xFFFF9800).copy(alpha = 0.1f),
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    if (item.category.isActive) "Active" else "Inactive",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.category.isActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.width(120.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==================== EMPTY STATES ====================

@Composable
fun EmptyCategoryState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // ✅ Dynamic
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "🏷️", fontSize = 56.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface // ✅ Dynamic
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start by adding your first category",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyMenuState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // ✅ Dynamic
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "📋", fontSize = 56.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Menu Items",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface // ✅ Dynamic
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start by adding your first menu item",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
            textAlign = TextAlign.Center
        )
    }
}

// ==================== PAGINATION ====================

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = if (currentPage > 1)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) // ✅ Dynamic
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val visiblePages = getVisiblePages(currentPage, totalPages)
        visiblePages.forEach { page ->
            if (page == -1) {
                Text(
                    "...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Dynamic
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = if (page == currentPage)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent,
                    onClick = { onPageChange(page) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$page",
                            fontSize = 14.sp,
                            fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                            color = if (page == currentPage)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant // ✅ Dynamic
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (currentPage < totalPages)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) // ✅ Dynamic
            )
        }
    }
}

// ==================== HELPERS ====================

fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "coffee" -> Color(0xFF6F4E37)
        "tea" -> Color(0xFF43A047)
        "snack" -> Color(0xFFFF9800)
        "dessert" -> Color(0xFFE91E63)
        "beverage", "drink" -> Color(0xFF2196F3)
        "food" -> Color(0xFFF44336)
        "breakfast" -> Color(0xFFFFA726)
        "lunch" -> Color(0xFF66BB6A)
        "dinner" -> Color(0xFF5C6BC0)
        else -> Color(0xFF9E9E9E)
    }
}

fun getVisiblePages(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) return (1..totalPages).toList()
    return when {
        currentPage <= 4 -> listOf(1, 2, 3, 4, 5, -1, totalPages)
        currentPage >= totalPages - 3 -> listOf(1, -1, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages)
        else -> listOf(1, -1, currentPage - 1, currentPage, currentPage + 1, -1, totalPages)
    }
}