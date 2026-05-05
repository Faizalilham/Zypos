package dev.faizal.core.common.pref


import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences khusus untuk menyimpan status onboarding.
 *
 * Disimpan di core:common agar bisa diakses dari module manapun
 * (app untuk routing, onboarding untuk mark sebagai selesai).
 */
@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** True kalau user sudah pernah menyelesaikan onboarding. */
    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_COMPLETED, value).apply()

    /** Reset — berguna untuk testing atau saat user logout. */
    fun reset() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "zypos_onboarding_prefs"
        private const val KEY_COMPLETED = "onboarding_completed"
    }
}