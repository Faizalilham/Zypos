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
import dev.faizal.features.onboarding.domain.model.FnbType
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.AboutQuestionConfig
import dev.faizal.features.onboarding.ui.OnboardingScaffold
import dev.faizal.features.onboarding.ui.component.ChoiceCard
import dev.faizal.features.onboarding.ui.component.ChoiceCardLayout

/**
 * Step 1: Jenis F&B (single-select).
 */
@Composable
fun FnbTypeScreen(
    selectedType: FnbType?,
    isAboutExpanded: Boolean,
    onToggleAbout: () -> Unit,
    screenConfig: ScreenConfig,
    onSelectType: (FnbType) -> Unit,
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
        step = OnboardingStep.FNB_TYPE,
        title = "Jenis F&B apa\nyang Anda jalankan?",
        onBack = onBack,
        onNext = onNext,
        nextEnabled = selectedType != null,
        aboutQuestion = AboutQuestionConfig(
            summary = "Jenis F&B menentukan template menu...",
            fullExplanation = "Jenis F&B menentukan template menu, kategori " +
                    "default, dan rekomendasi fitur yang akan kami siapkan. " +
                    "Misal: Cafe akan dapat opsi size & temperature otomatis, " +
                    "Restoran akan dapat fitur table management lebih detail.",
        ),
        isAboutExpanded = isAboutExpanded,
        onToggleAbout = onToggleAbout,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(FnbType.entries) { type ->
                ChoiceCard(
                    title = type.label,
                    subtitle = type.description,
                    emoji = type.emoji,
                    isSelected = type == selectedType,
                    onClick = { onSelectType(type) },
                    layout = cardLayout,
                )
            }
        }
    }
}