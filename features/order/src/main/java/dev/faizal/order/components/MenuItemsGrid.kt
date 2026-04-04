package dev.faizal.order.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.model.order.Order

@Composable
fun MenuItemsGrid(
    menus: List<Menu>,
    orderItems: List<Order>,
    isTabletPortrait: Boolean,
    isTabletLandscape: Boolean = false,
    selectedCategory: String?,
    searchQuery: String,
    onAddToCart: (Menu) -> Unit,
    modifier: Modifier = Modifier,
    isPhone: Boolean = false
) {
    val columnsPerRow = when {
        isTabletLandscape -> 5
        isTabletPortrait -> 3
        isPhone -> 3
        else -> 3
    }

    val selectedItemIds = remember(orderItems) {
        orderItems.map { it.menu.id }.toSet()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            menus.isEmpty() -> {
                EmptySearchStateWithImage(
                    category = selectedCategory ?: "All",
                    searchQuery = searchQuery,
                    emoji = "🔍"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(menus.chunked(columnsPerRow)) { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { item ->
                                MenuItemCard(
                                    item = item,
                                    isSelected = selectedItemIds.contains(item.id),
                                    onAddToCart = { onAddToCart(item) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columnsPerRow - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchStateWithImage(
    iconRes: Int? = null,
    category: String,
    searchQuery: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(48.dp)
    ) {
        Surface(modifier = Modifier.size(120.dp), shape = RoundedCornerShape(60.dp),
            color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(contentAlignment = Alignment.Center) { Text(text = emoji, fontSize = 56.sp) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "No Results Found", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        if (searchQuery.isNotBlank()) {
            Text(text = "No menu matching \"$searchQuery\"", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        } else if (category != "All") {
            Text(text = "No items in \"$category\" category", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        } else {
            Text(text = "No menu items available", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}