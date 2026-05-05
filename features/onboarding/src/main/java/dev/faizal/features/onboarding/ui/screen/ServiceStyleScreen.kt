package dev.faizal.features.onboarding.ui.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.domain.model.ServiceStyle
import dev.faizal.features.onboarding.ui.AboutQuestionConfig
import dev.faizal.features.onboarding.ui.OnboardingScaffold
import dev.faizal.features.onboarding.ui.component.ChoiceCard
import dev.faizal.features.onboarding.ui.component.ChoiceCardLayout

/**
 * Step 3: Gaya layanan (dine-in / takeaway / both).
 * Pilihan ini akan auto-aktifkan/non-aktifkan fitur table management.
 */
@Composable
fun ServiceStyleScreen(
    selected: ServiceStyle?,
    isAboutExpanded: Boolean,
    onToggleAbout: () -> Unit,
    onSelect: (ServiceStyle) -> Unit,
    screenConfig: ScreenConfig,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {

    val columns = when {
        screenConfig.isTablet && screenConfig.isLandscape -> 4
        screenConfig.isTablet -> 3
        else -> 1
    }
    val cardLayout = if (screenConfig.isTablet) ChoiceCardLayout.Compact else ChoiceCardLayout.Horizontal

    OnboardingScaffold(
        step = OnboardingStep.SERVICE_STYLE,
        title = "Bagaimana gaya layanan\nutama warung Anda?",
        onBack = onBack,
        onNext = onNext,
        nextEnabled = selected != null,
        aboutQuestion = AboutQuestionConfig(
            summary = "Pilihan ini menentukan fitur dine-in & meja...",
            fullExplanation = "Pilihan ini menentukan apakah fitur manajemen meja " +
                    "& dine-in akan diaktifkan secara default. Untuk takeaway-only, " +
                    "kami akan menyederhanakan flow order tanpa pemilihan meja " +
                    "agar lebih cepat saat ramai.",
        ),
        isAboutExpanded = isAboutExpanded,
        onToggleAbout = onToggleAbout,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        )  {
            items(ServiceStyle.entries) { style ->
                ChoiceCard(
                    title = style.label,
                    subtitle = style.description,
                    emoji = style.emoji,
                    isSelected = style == selected,
                    onClick = { onSelect(style) },
                    layout = cardLayout
                )
            }
        }
    }
}