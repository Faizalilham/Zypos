package dev.faizal.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Splash : Screen()

    @Serializable
    data object Main : Screen()
}