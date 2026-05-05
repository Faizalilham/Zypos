package dev.faizal.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.common.utils.ScreenConfig
import dev.faizal.features.onboarding.OnboardingViewModel
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.ui.screen.CustomerCapacityScreen
import dev.faizal.features.onboarding.ui.screen.FnbTypeScreen
import dev.faizal.features.onboarding.ui.screen.PriorityFeatureScreen
import dev.faizal.features.onboarding.ui.screen.ServiceStyleScreen
import dev.faizal.features.onboarding.ui.screen.StoreInfoScreen
import dev.faizal.features.onboarding.ui.screen.WelcomeIntroScreen

/**
 * Entry point onboarding F&B. Routing antar step dilakukan di sini.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    screenConfig : ScreenConfig,
    onFinished: () -> Unit,
) {
    val state = viewModel.state

    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = {
                    if (targetState.ordinal > initialState.ordinal) it else -it
                },
            ) with slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = {
                    if (targetState.ordinal > initialState.ordinal) -it else it
                },
            )
        },
        label = "onboarding_step",
    ) { step ->
        when (step) {
            OnboardingStep.WELCOME_INTRO -> WelcomeIntroScreen(
                onStart = viewModel::goToNextStep,
            )

            OnboardingStep.FNB_TYPE -> FnbTypeScreen(
                selectedType = state.data.fnbType,
                isAboutExpanded = state.isAboutQuestionExpanded,
                onToggleAbout = viewModel::toggleAboutQuestion,
                onSelectType = viewModel::selectFnbType,
                onBack = viewModel::goToPreviousStep,
                onNext = viewModel::goToNextStep,
                screenConfig = screenConfig
            )

            OnboardingStep.CUSTOMER_CAPACITY -> CustomerCapacityScreen(
                selected = state.data.customerCapacity,
                isAboutExpanded = state.isAboutQuestionExpanded,
                onToggleAbout = viewModel::toggleAboutQuestion,
                onSelect = viewModel::selectCustomerCapacity,
                onBack = viewModel::goToPreviousStep,
                onNext = viewModel::goToNextStep,
                screenConfig = screenConfig
            )

            OnboardingStep.SERVICE_STYLE -> ServiceStyleScreen(
                selected = state.data.serviceStyle,
                isAboutExpanded = state.isAboutQuestionExpanded,
                onToggleAbout = viewModel::toggleAboutQuestion,
                onSelect = viewModel::selectServiceStyle,
                onBack = viewModel::goToPreviousStep,
                onNext = viewModel::goToNextStep,
                screenConfig = screenConfig
            )

            OnboardingStep.STORE_INFO -> StoreInfoScreen(
                storeName = state.data.storeName,
                storeAddress = state.data.storeAddress,
                storePhone = state.data.storePhone,
                isAboutExpanded = state.isAboutQuestionExpanded,
                onToggleAbout = viewModel::toggleAboutQuestion,
                onStoreNameChange = viewModel::updateStoreName,
                onStoreAddressChange = viewModel::updateStoreAddress,
                onStorePhoneChange = viewModel::updateStorePhone,
                onBack = viewModel::goToPreviousStep,
                onNext = viewModel::goToNextStep,
            )

            OnboardingStep.OPERATIONAL -> {
                viewModel.goToNextStep()
            }

            OnboardingStep.PRIORITY_FEATURE -> PriorityFeatureScreen(
                selectedFeatures = state.data.priorityFeatures,
                isAboutExpanded = state.isAboutQuestionExpanded,
                onToggleAbout = viewModel::toggleAboutQuestion,
                onToggleFeature = viewModel::togglePriorityFeature,
                onBack = viewModel::goToPreviousStep,
                onNext = viewModel::goToNextStep,
                screenConfig = screenConfig
            )

            OnboardingStep.PERMISSION_NOTIFICATION -> {
                viewModel.goToNextStep()
            }

            OnboardingStep.REGISTER -> {
                viewModel.finishOnboarding(onFinished)
            }
        }
    }
}