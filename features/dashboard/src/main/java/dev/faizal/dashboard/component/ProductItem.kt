package dev.faizal.dashboard.component

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.dashboard.Product

@Composable
fun ProductItem(product: Product) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Product Name and Category (weight 1f, center aligned)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Category with background
            val (categoryColor, backgroundColor) = when(product.category) {
                "Pastry" -> Color(0xFF00BCD4) to Color(0xFF00BCD4).copy(alpha = 0.15f)
                "Sandwich" -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.15f)
                "Cake" -> Color(0xFFE91E63) to Color(0xFFE91E63).copy(alpha = 0.15f)
                else -> Color.Gray to Color.Gray.copy(alpha = 0.15f)
            }

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = backgroundColor
            ) {
                Text(
                    product.category,
                    fontSize = 11.sp,
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Total Orders (wrap content)
        Text(
            "${product.orders} Times",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}