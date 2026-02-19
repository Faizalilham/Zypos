package dev.faizal.features.menu.components.category

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
import dev.faizal.features.menu.CategoryViewModel
import dev.faizal.features.menu.CategoryWithCount
import dev.faizal.ui.component.EmptyState
import dev.faizal.ui.component.PaginationBar

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories (${categoriesWithCount.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface 
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
                EmptyState(
                    emoji = "🏷️",
                    title = "No Categories",
                    subtitle = "Start by adding your first category"
                )
            } else {
                // Table Header
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
                        Text(
                            "No",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
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