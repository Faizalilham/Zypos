package dev.faizal.zypos.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimplePieChart(
    data: List<Pair<String, Int>>, // Pair<ProductName, OrderCount>
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }

    if (total == 0) return

    // Generate colors for each slice
    val colors = listOf(
        Color(0xFF2196F3), // Biru
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Hijau
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFFFFEB3B), // Kuning
        Color(0xFF00BCD4), // Cyan
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val centerX = size.width / 2
            val centerY = size.height / 2

            var currentAngle = -90f // Start from top

            data.forEachIndexed { index, (_, count) ->
                val percentage = count.toFloat() / total
                val sweepAngle = 360f * percentage

                drawArc(
                    color = colors[index % colors.size],
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )

                // ✅ Draw percentage text on each slice
                if (percentage > 0.08) { // Only show if slice is large enough (>8%)
                    val angleInRadians = Math.toRadians((currentAngle + sweepAngle / 2).toDouble())
                    val textRadius = radius * 0.65f // Position text at 65% of radius
                    val textX = centerX + (textRadius * kotlin.math.cos(angleInRadians)).toFloat()
                    val textY = centerY + (textRadius * kotlin.math.sin(angleInRadians)).toFloat()

                    val percentageText = "${String.format("%.0f", percentage * 100)}%"

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 28f
                            isFakeBoldText = true
                            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                        }
                        drawText(
                            percentageText,
                            textX,
                            textY + 10f, // Adjust vertical position
                            paint
                        )
                    }
                }

                currentAngle += sweepAngle
            }

            // Draw white center circle (donut effect)
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

@Composable
fun PieChartLegend(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF2196F3),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFFFFEB3B),
        Color(0xFF00BCD4),
    )

    val total = data.sumOf { it.second }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.take(5).forEachIndexed { index, (name, count) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )

                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Text(
                    text = "${String.format("%.1f", (count.toFloat() / total) * 100)}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}