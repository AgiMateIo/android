package ru.agimate.android.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ru.agimate.android.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    data object Triggers : Screen(
        route = "triggers",
        titleResId = R.string.nav_triggers,
        icon = Icons.Default.Bolt
    )

    data object Actions : Screen(
        route = "actions",
        titleResId = R.string.nav_actions,
        icon = Icons.Default.PlayArrow
    )

    data object Settings : Screen(
        route = "settings",
        titleResId = R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(Triggers, Actions, Settings)
    }
}
