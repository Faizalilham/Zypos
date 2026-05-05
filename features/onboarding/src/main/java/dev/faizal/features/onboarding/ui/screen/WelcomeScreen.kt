package dev.faizal.features.onboarding.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.utils.backgroundColor

/**
 * Welcome screen pertama untuk user F&B baru ZyPos.
 * Tidak ada progress bar, fokus ke first impression.
 */
@Composable
fun WelcomeIntroScreen(
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingStep.WELCOME_INTRO.backgroundColor())
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Placeholder maskot — ganti dengan ilustrasi asli ZyPos
            MascotPlaceholder()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Selamat datang\ndi ZyPos",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aplikasi kasir khusus untuk warung, cafe, " +
                        "dan restoran. Bantu UMKM F&B Anda berkembang!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Mulai Sekarang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MascotPlaceholder() {
    Surface(
        modifier = Modifier.size(200.dp),
        shape = RoundedCornerShape(100.dp),
        color = Color.White.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "☕",
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}