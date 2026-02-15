package dev.faizal.zypos.ui.screens.order.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
fun SerratedContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface, // ✅ Dynamic default
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val waveRadius = 8.dp.toPx()
            val waveDiameter = waveRadius * 2
            val waveGap = 8.dp.toPx()
            val cornerRadius = 12.dp.toPx()

            // PATH UNTUK SHADOW GERIGI
            val serratedShadowPath = Path().apply {
                moveTo(0f, waveRadius)

                var x = 0f
                val flatWidth = waveRadius * 0.6f
                while (x < size.width) {
                    lineTo(x + waveRadius - flatWidth/2, 0f)
                    lineTo(x + waveRadius + flatWidth/2, 0f)
                    lineTo(x + waveDiameter, waveRadius)
                    lineTo(x + waveDiameter + waveGap, waveRadius)
                    x += waveDiameter + waveGap
                }

                lineTo(0f, waveRadius)
                close()
            }

            // PATH UNTUK CONTAINER UTAMA
            val mainPath = Path().apply {
                moveTo(0f, waveRadius)

                var x = 0f
                val flatWidth = waveRadius * 0.6f
                while (x < size.width) {
                    lineTo(x + waveRadius - flatWidth/2, 0f)
                    lineTo(x + waveRadius + flatWidth/2, 0f)
                    lineTo(x + waveDiameter, waveRadius)
                    lineTo(x + waveDiameter + waveGap, waveRadius)
                    x += waveDiameter + waveGap
                }

                lineTo(size.width, size.height - cornerRadius)
                arcTo(
                    rect = Rect(
                        size.width - cornerRadius * 2,
                        size.height - cornerRadius * 2,
                        size.width,
                        size.height
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(cornerRadius, size.height)
                arcTo(
                    rect = Rect(
                        0f,
                        size.height - cornerRadius * 2,
                        cornerRadius * 2,
                        size.height
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(0f, waveRadius)
                close()
            }

            // ✅ Shadow dengan alpha yang adaptif untuk dark mode
            drawPath(
                path = serratedShadowPath,
                color = Color.Black.copy(alpha = 0.15f), // Lebih subtle untuk dark mode
                style = Fill
            )

            drawPath(path = mainPath, backgroundColor)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}