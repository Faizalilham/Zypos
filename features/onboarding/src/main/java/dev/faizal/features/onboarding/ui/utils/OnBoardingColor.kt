package dev.faizal.features.onboarding.ui.utils


import androidx.compose.ui.graphics.Color
import dev.faizal.core.designsystem.OnboardingButter
import dev.faizal.core.designsystem.OnboardingLavender
import dev.faizal.core.designsystem.OnboardingLilac
import dev.faizal.core.designsystem.OnboardingMintGreen
import dev.faizal.core.designsystem.OnboardingPeach
import dev.faizal.core.designsystem.OnboardingPink
import dev.faizal.core.designsystem.OnboardingSkyBlue
import dev.faizal.core.designsystem.SurfaceGray
import dev.faizal.features.onboarding.domain.model.OnboardingStep

/**
 * Mapping warna background per step onboarding.
 * Sumber warna ada di core:designsystem agar konsisten dengan brand ZyPos.
 *
 * Untuk menambah/mengubah warna, edit file Color.kt di core:designsystem,
 * jangan hardcode di sini.
 */
fun OnboardingStep.backgroundColor(): Color = when (this) {
    OnboardingStep.WELCOME_INTRO -> OnboardingLavender
    OnboardingStep.STORE_INFO -> OnboardingSkyBlue
    OnboardingStep.OPERATIONAL -> OnboardingLilac
    OnboardingStep.PRIORITY_FEATURE -> OnboardingMintGreen
    OnboardingStep.PERMISSION_NOTIFICATION -> OnboardingPeach
    OnboardingStep.REGISTER -> SurfaceGray
    OnboardingStep.FNB_TYPE -> SurfaceGray
    OnboardingStep.CUSTOMER_CAPACITY -> SurfaceGray
    OnboardingStep.SERVICE_STYLE -> SurfaceGray
}