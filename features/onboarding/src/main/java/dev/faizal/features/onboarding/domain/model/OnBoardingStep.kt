package dev.faizal.features.onboarding.domain.model

/**
 * Step urutan dalam flow onboarding F&B.
 * displayIndex = nomor step yang ditampilkan di progress bar (-1 = tidak masuk progress).
 */
enum class OnboardingStep(val displayIndex: Int, val totalDataSteps: Int = 6) {
    // Phase 1: Welcome (tidak masuk progress bar)
    WELCOME_INTRO(-1),

    // Phase 2: Data Collection (6 steps)
    FNB_TYPE(1),
    CUSTOMER_CAPACITY(2),
    SERVICE_STYLE(3),
    STORE_INFO(4),
    OPERATIONAL(5),
    PRIORITY_FEATURE(6),

    // Phase 3: Permission
    PERMISSION_NOTIFICATION(-1),

    // Phase 4: Register
    REGISTER(-1),
    ;

    fun isDataCollectionStep() = displayIndex > 0
}