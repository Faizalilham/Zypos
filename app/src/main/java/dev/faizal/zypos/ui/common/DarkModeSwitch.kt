package dev.faizal.zypos.ui.common


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.faizal.zypos.R
import dev.faizal.zypos.ui.theme.ZyposTheme
import kotlinx.coroutines.launch



val BlueSky= Color(0xFF4478a9)
val NightSky =  Color(0xFF333333)
val BorderColor = Color(0x40000000)

@Composable
fun DarkModeSwitch(checked: Boolean, modifier: Modifier = Modifier, onCheckedChanged: (Boolean) -> Unit) {

    // Ukuran diperkecil maksimal
    val switchWidth = 70.dp
    val switchHeight = 36.dp
    val handleSize = 28.dp
    val handlePadding = 4.dp

    val valueToOffset = if (checked) 1f else 0f
    val offset = remember { Animatable(valueToOffset) }
    val scope = rememberCoroutineScope()

    DisposableEffect(checked) {
        if (offset.targetValue != valueToOffset) {
            scope.launch {
                offset.animateTo(valueToOffset, animationSpec = tween(800))  // Animasi lebih cepat
            }
        }
        onDispose { }
    }

    // Box luar untuk area klik
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(switchWidth + 24.dp)  // Padding klik horizontal
            .height(switchHeight + 12.dp) // Padding klik vertical
            .toggleable(
                value = checked,
                onValueChange = onCheckedChanged,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        // Box dalam untuk visual switch
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .width(switchWidth)
                .height(switchHeight)
                .clip(RoundedCornerShape(switchHeight))
                .background(lerp(BlueSky, NightSky, offset.value))
                .border(1.5.dp, BorderColor, RoundedCornerShape(switchHeight))  // Border lebih tipis
        ) {
            val backgroundPainter = painterResource(R.drawable.background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                with(backgroundPainter) {
                    val scale = size.width / intrinsicSize.width
                    val scaledHeight = intrinsicSize.height * scale
                    translate(top = (size.height - scaledHeight) * (1f - offset.value)) {
                        draw(Size(size.width, scaledHeight))
                    }
                }
            }

            Image(
                painter = painterResource(R.drawable.glow),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(switchWidth)
                    .graphicsLayer {
                        scaleX = 1.2f
                        scaleY = scaleX
                        translationX = lerp(
                            -size.width * 0.5f + handlePadding.toPx() + handleSize.toPx() * 0.5f,
                            switchWidth.toPx() - size.width * 0.5f - handlePadding.toPx() - handleSize.toPx() * 0.5f,
                            offset.value
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = handlePadding)
                    .size(handleSize)
                    .offset(x = (switchWidth - handleSize - handlePadding * 2f) * offset.value)
                    .paint(painterResource(R.drawable.sun))
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.moon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(handleSize)
                        .graphicsLayer {
                            translationX = size.width * (1f - offset.value)
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DarkModeSwitchPreview() {
    ZyposTheme {
        Column {
            var value by remember { mutableStateOf(true) }
            DarkModeSwitch(value, Modifier.padding(24.dp)) { value = it }
            DarkModeSwitch(!value, Modifier.padding(24.dp)) { value = !it }
        }
    }
}