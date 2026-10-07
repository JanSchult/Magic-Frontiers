package com.example.magicfrontiers.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.magicfrontiers.ui.screens.FactionSelectScreen
import com.example.magicfrontiers.ui.screens.GameScreen
import com.example.magicfrontiers.ui.screens.MainMenuScreen

@Composable
fun GameNavGraph(modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu,
        modifier= modifier
    ) {

        composable<Screen.MainMenu> {
            MainMenuScreen(
                onNewGame = { navController.navigate(Screen.FactionSelect) }
            )
        }

        composable<Screen.FactionSelect> {
            FactionSelectScreen(
                onFactionChosen = { factionId ->
                    navController.navigate(Screen.Game(factionId)) {
                        popUpTo(Screen.MainMenu) // zurück zum Menü, nicht zur Auswahl
                    }
                }
            )
        }

        composable<Screen.Game> { backStackEntry ->
            val gameRoute: Screen.Game = backStackEntry.toRoute()
            GameScreen(
                playerFactionId = gameRoute.factionId
            )
        }
    }
}