package dev.faizal.features.menu.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.faizal.core.common.utils.getCategoryColor
import dev.faizal.core.common.utils.toCurrencyString
import dev.faizal.core.domain.model.menu.Menu

@Composable
fun MenuTableRow(
    item: Menu,
    actualIndex: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLandscape: Boolean = false,
    isPhone: Boolean = false
) {
    if (isPhone) {
        // Phone: Card layout
        val categoryColor = getCategoryColor(item.categoryName)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Menu Image",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.basePrice.toCurrencyString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Category badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = categoryColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = item.categoryName,
                                fontSize = 10.sp,
                                color = categoryColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.isActive) Color(0xFF4CAF50).copy(alpha = 0.12f)
                            else Color(0xFFFF9800).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (item.isActive) "Active" else "Inactive",
                                fontSize = 10.sp,
                                color = if (item.isActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    } else {
        // Tablet: Table row (tidak berubah)
        val categoryColor = getCategoryColor(item.categoryName)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$actualIndex", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(if (isLandscape) 35.dp else 40.dp))

                Box(
                    modifier = Modifier
                        .width(if (isLandscape) 60.dp else 70.dp)
                        .height(if (isLandscape) 45.dp else 50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (!item.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUri).crossfade(true).build(),
                            contentDescription = null,
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
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Text(item.name, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(0.35f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                    Surface(shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.15f)) {
                        Text(item.categoryName, fontSize = 11.sp,
                            fontWeight = FontWeight.Medium, color = categoryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            textAlign = TextAlign.Center, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(item.basePrice.toCurrencyString(), fontSize = if (isLandscape) 13.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(0.25f), textAlign = TextAlign.End)

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
                    Surface(shape = RoundedCornerShape(12.dp),
                        color = if (item.isActive) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else Color(0xFFFF9800).copy(alpha = 0.1f)) {
                        Text(if (item.isActive) "Active" else "Inactive", fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (item.isActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(modifier = Modifier.width(if (isLandscape) 80.dp else 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFF44336),
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}