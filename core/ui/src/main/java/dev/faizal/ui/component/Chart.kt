package dev.faizal.ui.component

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.core.common.utils.toPercentageString
import dev.faizal.core.designsystem.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

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
                    size = Size(radius * 2, radius * 2)
                )

                // ✅ Draw percentage text on each slice
                if (percentage > 0.08) { // Only show if slice is large enough (>8%)
                    val angleInRadians = Math.toRadians((currentAngle + sweepAngle / 2).toDouble())
                    val textRadius = radius * 0.65f // Position text at 65% of radius
                    val textX = centerX + (textRadius * cos(angleInRadians)).toFloat()
                    val textY = centerY + (textRadius * sin(angleInRadians)).toFloat()

                    val percentageText = percentage.toPercentageString(decimals = 0)

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = Paint.Align.CENTER
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
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = (count.toFloat() / total).toPercentageString(decimals = 1),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WaveChart(
    dataPoints: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    var selectedDataPoint by remember { mutableStateOf<Pair<Int, Float>?>(null) }
    var isHovering by remember { mutableStateOf(false) }

    // ✅ Gunakan dataPoints dari parameter, bukan generate random
    val fullDataPoints = remember(dataPoints) {
        dataPoints.ifEmpty {
            // Default data jika kosong
            List(30) { day ->
                Pair((day + 1).toString(), 0f)
            }
        }
    }

    val itemWidth = 80.dp

    Box(modifier = modifier) {
        if (fullDataPoints.all { it.second == 0f }) {
            // ✅ Empty state jika semua data 0
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Sales Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width(itemWidth * fullDataPoints.size)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Move, PointerEventType.Enter -> {
                                            isHovering = true
                                            val position = event.changes.first().position
                                            val spacing = size.width / (fullDataPoints.size - 1).coerceAtLeast(1)
                                            val index = ((position.x / spacing).toInt())
                                                .coerceIn(0, fullDataPoints.size - 1)
                                            selectedDataPoint = Pair(index, fullDataPoints[index].second)
                                        }
                                        PointerEventType.Exit -> {
                                            isHovering = false
                                            selectedDataPoint = null
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    if (fullDataPoints.size < 2) return@Canvas

                    val spacing = width / (fullDataPoints.size - 1)

                    val minValue = fullDataPoints.minOf { it.second }
                    val maxValue = fullDataPoints.maxOf { it.second }
                    val valueRange = (maxValue - minValue).coerceAtLeast(1f)

                    val wavePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, height)

                    val wavePoints = mutableListOf<Offset>()

                    fullDataPoints.forEachIndexed { index, (_, value) ->
                        val x = index * spacing
                        val normalizedValue = (value - minValue) / valueRange
                        val y = height - (normalizedValue * height * 0.7f) - (height * 0.15f)

                        wavePoints.add(Offset(x, y))
                    }

                    // Create smooth curve
                    wavePoints.forEachIndexed { index, point ->
                        if (index == 0) {
                            wavePath.moveTo(point.x, point.y)
                            fillPath.lineTo(point.x, point.y)
                        } else {
                            val prevPoint = wavePoints[index - 1]

                            val p0 = if (index > 1) wavePoints[index - 2] else prevPoint
                            val p1 = prevPoint
                            val p2 = point
                            val p3 = if (index < wavePoints.size - 1) wavePoints[index + 1] else point

                            val tension = 1.0f

                            val controlX1 = p1.x + (p2.x - p0.x) * tension / 6
                            val controlY1 = p1.y + (p2.y - p0.y) * tension / 6
                            val controlX2 = p2.x - (p3.x - p1.x) * tension / 6
                            val controlY2 = p2.y - (p3.y - p1.y) * tension / 6

                            wavePath.cubicTo(
                                controlX1, controlY1,
                                controlX2, controlY2,
                                point.x, point.y
                            )
                            fillPath.cubicTo(
                                controlX1, controlY1,
                                controlX2, controlY2,
                                point.x, point.y
                            )
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF2196F3).copy(alpha = 0.4f),
                                0.3f to Color(0xFF2196F3).copy(alpha = 0.25f),
                                0.6f to Color(0xFF2196F3).copy(alpha = 0.15f),
                                0.8f to Color(0xFF2196F3).copy(alpha = 0.05f),
                                1.0f to Color(0xFF2196F3).copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw wave line with shadow
                    drawPath(
                        path = wavePath,
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    drawPath(
                        path = wavePath,
                        color = Color(0xFF2196F3),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw hover effects
                    selectedDataPoint?.let { (selectedIndex, _) ->
                        if (selectedIndex < wavePoints.size) {
                            val point = wavePoints[selectedIndex]

                            drawLine(
                                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                start = Offset(point.x, 0f),
                                end = Offset(point.x, height),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f)
                                )
                            )

                            drawCircle(
                                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                radius = 12.dp.toPx(),
                                center = point
                            )

                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = point
                            )

                            drawCircle(
                                color = Color(0xFF2196F3),
                                radius = 5.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // Tooltip
            if (isHovering) {
                selectedDataPoint?.let { (index, value) ->
                    if (index < fullDataPoints.size) {
                        val point = fullDataPoints[index]

                        val currentMonth = LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("MMMM"))

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            TooltipPopup(
                                label = "$currentMonth ${point.first}",
                                value = value,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            // Scroll indicator
            if (fullDataPoints.size > 10) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_up),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                "Scroll",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Icon(
                                painter = painterResource(R.drawable.arrow_down),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}