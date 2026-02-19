package dev.faizal.features.menu.components.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.features.menu.CategoryWithCount

@Composable
fun CategoryTableRow(
    item: CategoryWithCount,
    actualIndex: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            Text(
                "$actualIndex",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier.width(50.dp)
            )
            Text(item.category.emoji, fontSize = 24.sp, modifier = Modifier.width(80.dp))
            Text(
                item.category.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface, 
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