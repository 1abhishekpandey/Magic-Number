package com.abhishek.magicnumber.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhishek.magicnumber.ui.screens.GameScreen
import com.abhishek.magicnumber.ui.screens.HomeScreen
import com.abhishek.magicnumber.ui.screens.SettingsScreen

/**
 * Defines the navigation routes in the app.
 *
 * Using sealed class for type-safe navigation routes.
 */
sealed class Screen(val route: String) {
    /** Home screen with Start button */
    data object Home : Screen("home")

    /** Game screen where user swipes through cards */
    data object Game : Screen("game")

    /** Settings screen for configuring range and layout */
    data object Settings : Screen("settings")
}

/**
 * Main navigation host for the app.
 *
 * @param navController Navigation controller for managing navigation state
 * @param modifier Modifier for the NavHost container
 */
@Composable
fun MagicNumberNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartClick = {
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPlayAgain = {
                    // Pop back to home, then navigate to game again for fresh state
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                    navController.navigate(Screen.Game.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
