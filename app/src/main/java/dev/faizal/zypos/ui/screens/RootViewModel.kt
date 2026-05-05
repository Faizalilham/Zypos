package dev.faizal.zypos.ui.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.common.pref.OnboardingPreferences
import javax.inject.Inject
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
/**
 * ViewModel ringan untuk cek & update status onboarding dari RootNavGraph.
 * Dipakai untuk menentukan destination setelah splash.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    fun isOnboardingCompleted(): Boolean =
        onboardingPreferences.isOnboardingCompleted

    fun markOnboardingCompleted() {
        onboardingPreferences.isOnboardingCompleted = true
    }

    fun checkUpdate(activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                )
            }
        }
    }

}