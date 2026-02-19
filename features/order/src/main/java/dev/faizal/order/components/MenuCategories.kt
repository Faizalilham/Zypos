package dev.faizal.order.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.domain.model.menu.Category

@Composable
fun MenuCategories(
    categories: List<Category>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {

    val allCategories = listOf(
        Category(
            id = 0,
            name = "All Menu",
            emoji = "🍽️",
            displayOrder = 0,
            isActive = true
        )
    ) + categories

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(allCategories) { category ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .height(60.dp)
                    .clickable { onCategorySelected(category.name) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (category.name == selectedCategory)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface 
                ),
                border = BorderStroke(
                    width = if (category.name == selectedCategory) 2.dp else 1.dp,
                    color = if (category.name == selectedCategory)
                        MaterialTheme.colorScheme.primary 
                    else
                        MaterialTheme.colorScheme.outline 
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (category.name == selectedCategory) 2.dp else 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = category.emoji,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (category.name == selectedCategory)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface 
                        )
                    }
                }
            }
        }
    }
}