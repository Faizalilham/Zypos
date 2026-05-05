package dev.faizal.features.onboarding.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.faizal.core.designsystem.PrimaryBlue
import dev.faizal.core.designsystem.ProgressBarDone
import dev.faizal.core.designsystem.SurfaceWhite

/**
 * Progress bar segmented yang menampilkan progress step onboarding.
 *
 * Status warna (semua dari design system):
 * - DONE: ProgressBarDone (AccentGreen) dengan ikon check
 * - ACTIVE: PrimaryBlue
 * - INACTIVE: SurfaceWhite transparan
 */
@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val status = when {
                stepNumber < currentStep -> SegmentStatus.DONE
                stepNumber == currentStep -> SegmentStatus.ACTIVE
                else -> SegmentStatus.INACTIVE
            }
            ProgressSegment(
                status = status,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProgressSegment(
    status: SegmentStatus,
    modifier: Modifier = Modifier,
) {
    val targetColor = when (status) {
        SegmentStatus.DONE -> ProgressBarDone
        SegmentStatus.ACTIVE -> PrimaryBlue
        SegmentStatus.INACTIVE -> SurfaceWhite.copy(alpha = 0.5f)
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        label = "segment_color",
    )

    Row(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(animatedColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (status == SegmentStatus.DONE) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(2.dp)
                    .height(6.dp),
            )
        }
    }
}

private enum class SegmentStatus { DONE, ACTIVE, INACTIVE }