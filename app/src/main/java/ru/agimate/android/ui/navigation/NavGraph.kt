package ru.agimate.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.agimate.android.ui.screens.actions.ActionsScreen
import ru.agimate.android.ui.screens.settings.SettingsScreen
import ru.agimate.android.ui.screens.triggers.TriggersScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Triggers.route,
        modifier = modifier
    ) {
        composable(Screen.Triggers.route) {
            TriggersScreen()
        }

        composable(Screen.Actions.route) {
            ActionsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
