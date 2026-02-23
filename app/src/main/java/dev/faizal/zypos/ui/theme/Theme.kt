package dev.faizal.zypos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.faizal.core.designsystem.AccentGreen
import dev.faizal.core.designsystem.BackgroundLight
import dev.faizal.core.designsystem.BackgroundWhite
import dev.faizal.core.designsystem.BorderLight
import dev.faizal.core.designsystem.BrownPrimary
import dev.faizal.core.designsystem.PrimaryBlue
import dev.faizal.core.designsystem.StatusError
import dev.faizal.core.designsystem.SurfaceGray
import dev.faizal.core.designsystem.SurfaceWhite
import dev.faizal.core.designsystem.TextPrimary
import dev.faizal.core.designsystem.TextSecondary
import dev.faizal.core.designsystem.Typography


private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = BackgroundWhite,
    secondary = BrownPrimary,
    onSecondary = BackgroundWhite,
    tertiary = AccentGreen,
    background = BackgroundLight,          // ✅ Light background
    surface = SurfaceWhite,                // ✅ White surface
    onBackground = TextPrimary,            // ✅ Dark text
    onSurface = TextPrimary,               // ✅ Dark text
    surfaceVariant = SurfaceGray,          // ✅ Light gray
    onSurfaceVariant = TextSecondary,      // ✅ Gray text
    outline = BorderLight,
    outlineVariant = BorderLight.copy(alpha = 0.5f),
    error = StatusError,
    onError = BackgroundWhite
)

// ✅ FIXED: Dark mode colors yang konsisten
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = BackgroundWhite,
    secondary = BrownPrimary,
    onSecondary = BackgroundWhite,
    tertiary = AccentGreen,
    background = Color(0xFF121212),        // ✅ Dark background
    surface = Color(0xFF1E1E1E),           // ✅ Dark surface (lebih terang dari background)
    onBackground = BackgroundWhite,        // ✅ White text on dark
    onSurface = BackgroundWhite,           // ✅ White text on dark surface
    surfaceVariant = Color(0xFF2C2C2C),    // ✅ Slightly lighter surface
    onSurfaceVariant = Color(0xFFB0B0B0),  // ✅ Gray text for dark mode
    outline = Color(0xFF3C3C3C),           // ✅ Dark border
    outlineVariant = Color(0xFF2C2C2C),    // ✅ Lighter dark border
    error = StatusError,
    onError = BackgroundWhite
)

@Composable
fun ZyposTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}