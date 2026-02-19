package dev.faizal.ui.navigation

import kotlinx.serialization.Serializable

sealed class MainRoute {

    @Serializable
    data object Overview : MainRoute()

    @Serializable
    data object Order : MainRoute()

    @Serializable
    data object TransactionAll : MainRoute()

    @Serializable
    data object TransactionSales : MainRoute()

    @Serializable
    data object Menu : MainRoute()
}