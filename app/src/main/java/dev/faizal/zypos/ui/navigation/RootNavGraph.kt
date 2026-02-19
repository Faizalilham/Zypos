package dev.faizal.zypos.ui.navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.faizal.ui.navigation.Screen
import dev.faizal.zypos.ui.screens.MainNavigation
import dev.faizal.zypos.ui.screens.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootNavGraph(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash>(
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(500)
                )
            }
        ) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Main>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500)
                )
            }
        ) {
            MainNavigation(
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange
            )
        }
    }
}

