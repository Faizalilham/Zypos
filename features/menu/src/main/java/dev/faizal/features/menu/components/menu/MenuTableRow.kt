package dev.faizal.features.menu.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    isLandscape: Boolean = false
) {
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
            // No
            Text(
                text = "$actualIndex",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, 
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
                            .background(MaterialTheme.colorScheme.surfaceVariant), 
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Menu Name
            Text(
                text = item.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, 
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
                text = item.basePrice.toCurrencyString(),
                fontSize = if (isLandscape) 13.sp else 14.sp,
                color = MaterialTheme.colorScheme.onSurface, 
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