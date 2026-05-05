package dev.faizal.zypos.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.common.pref.OnboardingPreferences
import javax.inject.Inject

/**
 * ViewModel ringan untuk cek & update status onboarding dari RootNavGraph.
 * Dipakai untuk menentukan destination setelah splash.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {

    fun isOnboardingCompleted(): Boolean =
        onboardingPreferences.isOnboardingCompleted

    fun markOnboardingCompleted() {
        onboardingPreferences.isOnboardingCompleted = true
    }
}