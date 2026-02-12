package ru.agimate.android.data.model

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import ru.agimate.android.R

data class ActionDefinition(
    val type: ActionType,
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int,
    val icon: ImageVector,
    val requiredPermissions: List<String> = emptyList()
) {
    companion object {
        val ALL_ACTIONS = listOf(
            ActionDefinition(
                type = ActionType.TTS,
                displayNameResId = R.string.action_tts,
                descriptionResId = R.string.action_desc_tts,
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                requiredPermissions = emptyList()
            ),
            ActionDefinition(
                type = ActionType.NOTIFICATION,
                displayNameResId = R.string.action_notification,
                descriptionResId = R.string.action_desc_notification,
                icon = Icons.Default.Notifications,
                requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyList()
                }
            )
        )
    }
}
