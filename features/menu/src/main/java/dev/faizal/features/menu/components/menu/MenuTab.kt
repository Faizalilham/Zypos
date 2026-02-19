package dev.faizal.features.menu.components.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.features.menu.MenuViewModel
import dev.faizal.ui.component.EmptyState
import dev.faizal.ui.component.PaginationBar

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
            containerColor = MaterialTheme.colorScheme.surface 
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
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu Items (${menus.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface 
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
                EmptyState(
                    emoji = "📋",
                    title = "No Menu Items",
                    subtitle = "Start by adding your first menu item"
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant, 
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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