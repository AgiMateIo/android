package ru.agimate.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Triggers : Screen(
        route = "triggers",
        title = "Triggers",
        icon = Icons.Default.Bolt
    )

    data object Actions : Screen(
        route = "actions",
        title = "Actions",
        icon = Icons.Default.PlayArrow
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(Triggers, Actions, Settings)
    }
}
