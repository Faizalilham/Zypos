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
import dev.faizal.features.onboarding.domain.model.CustomerCapacity
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.AboutQuestionConfig
import dev.faizal.features.onboarding.ui.OnboardingScaffold
import dev.faizal.features.onboarding.ui.component.ChoiceCard
import dev.faizal.features.onboarding.ui.component.ChoiceCardLayout

/**
 * Step 2: Skala bisnis berdasarkan jumlah pelanggan harian.
 */
@Composable
fun CustomerCapacityScreen(
    selected: CustomerCapacity?,
    isAboutExpanded: Boolean,
    onToggleAbout: () -> Unit,
    onSelect: (CustomerCapacity) -> Unit,
    screenConfig : ScreenConfig,
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
        step = OnboardingStep.CUSTOMER_CAPACITY,
        title = "Berapa banyak pelanggan\nharian Anda?",
        onBack = onBack,
        onNext = onNext,
        nextEnabled = selected != null,
        aboutQuestion = AboutQuestionConfig(
            summary = "Skala harian menentukan optimasi performa...",
            fullExplanation = "Skala harian membantu kami menentukan optimasi " +
                    "performa aplikasi dan rekomendasi fitur. Warung yang baru " +
                    "mulai akan kami fokuskan pada kemudahan order, sedangkan " +
                    "yang sudah ramai akan kami siapkan dengan fitur quick action " +
                    "dan caching yang lebih agresif.",
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
            items(CustomerCapacity.entries) { capacity ->
                ChoiceCard(
                    title = capacity.label,
                    subtitle = capacity.description,
                    emoji = capacity.emoji,
                    isSelected = capacity == selected,
                    onClick = { onSelect(capacity) },
                    layout = cardLayout,
                )
            }
        }
    }
}