package dev.faizal.features.onboarding

import dev.faizal.features.onboarding.domain.model.OnboardingData
import dev.faizal.features.onboarding.domain.model.OnboardingStep

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME_INTRO,
    val data: OnboardingData = OnboardingData(),
    val isAboutQuestionExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)