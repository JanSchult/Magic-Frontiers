package com.example.magicfrontiers.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object MainMenu : Screen

    @Serializable
    data object FactionSelect : Screen

    @Serializable
    data class Game(val factionId: String) : Screen
}