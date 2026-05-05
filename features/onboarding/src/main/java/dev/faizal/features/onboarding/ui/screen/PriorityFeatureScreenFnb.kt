package dev.faizal.features.onboarding.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.features.onboarding.domain.model.FnbFeature
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.AboutQuestionConfig
import dev.faizal.features.onboarding.ui.OnboardingScaffold
import dev.faizal.features.onboarding.ui.component.ChoiceCard
import dev.faizal.features.onboarding.ui.component.ChoiceCardLayout

/**
 * Step 6: Fitur F&B prioritas (multi-select, max 3).
 */
@Composable
fun PriorityFeatureScreen(
    selectedFeatures: Set<FnbFeature>,
    isAboutExpanded: Boolean,
    onToggleAbout: () -> Unit,
    onToggleFeature: (FnbFeature) -> Unit,
    screenConfig: ScreenConfig,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val isValid = selectedFeatures.isNotEmpty() && selectedFeatures.size <= 3
    val columns = when {
        screenConfig.isTablet && screenConfig.isLandscape -> 4
        screenConfig.isTablet -> 3
        else -> 1
    }
    val cardLayout = if (screenConfig.isTablet) ChoiceCardLayout.Compact else ChoiceCardLayout.Horizontal

    OnboardingScaffold(
        step = OnboardingStep.PRIORITY_FEATURE,
        title = "Fitur F&B mana yang\npaling Anda butuhkan?",
        onBack = onBack,
        onNext = onNext,
        nextEnabled = isValid,
        nextLabel = "Selesai",
        aboutQuestion = AboutQuestionConfig(
            summary = "Fitur prioritas akan tampil di dashboard...",
            fullExplanation = "Fitur prioritas akan kami tampilkan paling depan " +
                    "di dashboard untuk akses cepat. Pilih maksimal 3 fitur " +
                    "yang paling sering Anda gunakan dalam operasional sehari-hari.",
        ),
        isAboutExpanded = isAboutExpanded,
        onToggleAbout = onToggleAbout,
    ) {
        Text(
            text = "Pilih maksimal 3 fitur (${selectedFeatures.size}/3)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(FnbFeature.entries) { feature ->
                ChoiceCard(
                    title = feature.label,
                    emoji = feature.emoji,
                    isSelected = feature in selectedFeatures,
                    onClick = { onToggleFeature(feature) },
                    showCheckmark = true,
                    layout = cardLayout
                )
            }
        }
    }
}