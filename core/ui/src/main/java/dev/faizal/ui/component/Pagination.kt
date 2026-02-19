package dev.faizal.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.common.utils.getVisiblePages

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
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) 
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val visiblePages = getVisiblePages(currentPage, totalPages)
        visiblePages.forEach { page ->
            if (page == -1) {
                Text(
                    "...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
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
                                MaterialTheme.colorScheme.onSurfaceVariant 
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
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) 
            )
        }
    }
}