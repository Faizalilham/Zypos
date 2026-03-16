package dev.faizal.zypos.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.faizal.zypos.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val isDark = isSystemInDarkTheme()

    // Background color mengikuti dark/light mode seperti Gojek
    val backgroundColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val subtitleColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)
    val fromTextColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)
    val companyColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF212121)

    // Fade-in animation
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = dev.faizal.core.designsystem.R.drawable.logo_zypos),
                contentDescription = "ZyPOS Logo",
                modifier = Modifier.width(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ZyPOS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = fromTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "PT Adunk Tbk.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = companyColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen {}
}