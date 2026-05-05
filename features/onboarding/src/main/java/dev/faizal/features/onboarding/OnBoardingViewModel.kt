package dev.faizal.features.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.common.pref.OnboardingPreferences
import dev.faizal.features.onboarding.domain.model.CustomerCapacity
import dev.faizal.features.onboarding.domain.model.FnbFeature
import dev.faizal.features.onboarding.domain.model.FnbType
import dev.faizal.features.onboarding.domain.model.OnboardingStep
import dev.faizal.features.onboarding.domain.model.ServiceStyle
import dev.faizal.features.onboarding.domain.usecase.SaveOnboardingUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.compareTo

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val saveOnboardingUseCase: SaveOnboardingUseCase,
) : ViewModel() {

    var state by mutableStateOf(OnboardingState())
        private set

    // ==================== NAVIGATION ====================

    fun goToNextStep() {
        val next = nextStepFor(state.currentStep) ?: return
        state = state.copy(
            currentStep = next,
            isAboutQuestionExpanded = false,
        )
    }

    fun goToPreviousStep() {
        val prev = previousStepFor(state.currentStep) ?: return
        state = state.copy(
            currentStep = prev,
            isAboutQuestionExpanded = false,
        )
    }

    fun toggleAboutQuestion() {
        state = state.copy(isAboutQuestionExpanded = !state.isAboutQuestionExpanded)
    }

    private fun nextStepFor(current: OnboardingStep): OnboardingStep? {
        val all = OnboardingStep.entries
        val idx = all.indexOf(current)
        return all.getOrNull(idx + 1)
    }

    private fun previousStepFor(current: OnboardingStep): OnboardingStep? {
        val all = OnboardingStep.entries
        val idx = all.indexOf(current)
        return all.getOrNull(idx - 1)
    }

    // ==================== STEP UPDATERS ====================

    fun selectFnbType(type: FnbType) {
        state = state.copy(data = state.data.copy(fnbType = type))
    }

    fun selectCustomerCapacity(capacity: CustomerCapacity) {
        state = state.copy(data = state.data.copy(customerCapacity = capacity))
    }

    fun selectServiceStyle(style: ServiceStyle) {
        // Auto-aktifkan fitur dine-in di priorityFeatures
        val newPriority = state.data.priorityFeatures.toMutableSet()
        if (style != ServiceStyle.TAKEAWAY_ONLY) {
            newPriority.add(FnbFeature.DINE_IN_TABLE)
        } else {
            newPriority.remove(FnbFeature.DINE_IN_TABLE)
        }

        state = state.copy(
            data = state.data.copy(
                serviceStyle = style,
                priorityFeatures = newPriority.take(3).toSet(),
            ),
        )
    }

    fun updateStoreName(name: String) {
        state = state.copy(data = state.data.copy(storeName = name))
    }

    fun updateStoreAddress(address: String) {
        state = state.copy(data = state.data.copy(storeAddress = address))
    }

    fun updateStorePhone(phone: String) {
        state = state.copy(data = state.data.copy(storePhone = phone))
    }

    fun updateStoreLogo(uri: String?) {
        state = state.copy(data = state.data.copy(storeLogoUri = uri))
    }

    fun updateOpenTime(time: String) {
        state = state.copy(data = state.data.copy(openTime = time))
    }

    fun updateCloseTime(time: String) {
        state = state.copy(data = state.data.copy(closeTime = time))
    }

    fun toggleTax(enabled: Boolean) {
        state = state.copy(data = state.data.copy(taxEnabled = enabled))
    }

    fun updateTaxPercentage(percentage: Double) {
        state = state.copy(data = state.data.copy(taxPercentage = percentage))
    }

    fun toggleServiceCharge(enabled: Boolean) {
        state = state.copy(data = state.data.copy(serviceChargeEnabled = enabled))
    }

    fun updateServiceChargePercentage(percentage: Double) {
        state = state.copy(data = state.data.copy(serviceChargePercentage = percentage))
    }

    fun togglePriorityFeature(feature: FnbFeature) {
        val current = state.data.priorityFeatures
        val newFeatures = if (current.contains(feature)) {
            current - feature
        } else {
            if (current.size >= 3) return
            current + feature
        }
        state = state.copy(data = state.data.copy(priorityFeatures = newFeatures))
    }

    fun setNotificationGranted(granted: Boolean) {
        state = state.copy(data = state.data.copy(notificationGranted = granted))
    }

    fun setStorageGranted(granted: Boolean) {
        state = state.copy(data = state.data.copy(storageGranted = granted))
    }

    // ==================== FINISH ====================

    /**
     * Save data ke Room via use case, mark onboarding done, lalu callback ke parent.
     * Kalau gagal save, set errorMessage tapi TIDAK mark done — biar user retry.
     */
    fun finishOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            saveOnboardingUseCase(state.data)
                .onSuccess {
                    onboardingPreferences.isOnboardingCompleted = true
                    state = state.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Gagal menyimpan pengaturan",
                    )
                }
        }
    }
}