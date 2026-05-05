package dev.faizal.zypos.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.faizal.core.common.utils.rememberScreenConfig
import dev.faizal.features.onboarding.ui.OnboardingScreen
import dev.faizal.ui.navigation.Screen
import dev.faizal.zypos.ui.screens.MainNavigation
import dev.faizal.zypos.ui.screens.RootViewModel
import dev.faizal.zypos.ui.screens.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootNavGraph(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val screenConfig = rememberScreenConfig()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
    ) {
        composable<Screen.Splash>(
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(500),
                )
            },
        ) {
            SplashScreen(
                onTimeout = {
                    // Routing setelah splash:
                    // - Belum pernah onboarding → ke Onboarding
                    // - Sudah pernah → langsung ke Main
                    val nextDestination = if (rootViewModel.isOnboardingCompleted()) {
                        Screen.Main
                    } else {
                        Screen.Onboarding
                    }
                    navController.navigate(nextDestination) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.Onboarding>(
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
        ) {
            OnboardingScreen(
                screenConfig = screenConfig,
                onFinished = {
                    rootViewModel.markOnboardingCompleted()
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.Main>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500),
                )
            },
        ) {
            MainNavigation(
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                screenConfig = screenConfig
            )
        }
    }
}