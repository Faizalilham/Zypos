package dev.faizal.features.onboarding.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import dev.faizal.features.onboarding.ui.component.AboutQuestionCard
import dev.faizal.features.onboarding.ui.component.OnboardingProgressBar
import dev.faizal.features.onboarding.ui.utils.backgroundColor

/**
 * Scaffold reusable untuk SEMUA step onboarding.
 *
 * Layout:
 * ┌─────────────────────────────┐
 * │  [Progress Bar (jika ada)]  │
 * │                             │
 * │  Title                      │
 * │  About Question Card        │
 * │                             │
 * │  [content area]             │
 * │                             │
 * │  [Back]   [Next/Continue]   │
 * └─────────────────────────────┘
 */
@Composable
fun OnboardingScaffold(
    step: OnboardingStep,
    title: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextLabel: String = "Selanjutnya",
    showProgressBar: Boolean = true,
    showBackButton: Boolean = true,
    aboutQuestion: AboutQuestionConfig? = null,
    isAboutExpanded: Boolean = false,
    onToggleAbout: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(step.backgroundColor())
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Progress bar di atas
            if (showProgressBar && step.isDataCollectionStep()) {
                OnboardingProgressBar(
                    currentStep = step.displayIndex,
                    totalSteps = step.totalDataSteps,
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                aboutQuestion?.let { config ->
                    AboutQuestionCard(
                        summary = config.summary,
                        fullExplanation = config.fullExplanation,
                        isExpanded = isAboutExpanded,
                        onToggle = onToggleAbout,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                content()
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (showBackButton) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.7f),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Button(
                    onClick = onNext,
                    enabled = nextEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text = nextLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

data class AboutQuestionConfig(
    val summary: String,
    val fullExplanation: String,
)