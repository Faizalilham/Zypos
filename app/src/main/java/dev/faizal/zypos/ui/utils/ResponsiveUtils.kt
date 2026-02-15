package dev.faizal.zypos.ui.utils


import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ScreenConfig(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isPhone: Boolean,
    val isTablet: Boolean,
    val isTabletPortrait: Boolean,
    val isLandscape: Boolean
)

@Composable
fun rememberScreenConfig(): ScreenConfig {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Phone: width < 600dp
    // Tablet: width >= 600dp
    val isPhone = screenWidth < 600.dp
    val isTablet = screenWidth >= 600.dp
    val isTabletPortrait = isTablet && !isLandscape

    return ScreenConfig(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        isPhone = isPhone,
        isTablet = isTablet,
        isTabletPortrait = isTabletPortrait,
        isLandscape = isLandscape
    )
}