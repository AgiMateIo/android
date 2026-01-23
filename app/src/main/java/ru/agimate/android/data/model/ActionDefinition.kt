package ru.agimate.android.data.model

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionDefinition(
    val type: ActionType,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val requiredPermissions: List<String> = emptyList()
) {
    companion object {
        val ALL_ACTIONS = listOf(
            ActionDefinition(
                type = ActionType.TTS,
                displayName = "Text-to-Speech",
                description = "Произнести текст с помощью TTS",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                requiredPermissions = emptyList()
            ),
            ActionDefinition(
                type = ActionType.NOTIFICATION,
                displayName = "Notification",
                description = "Показать системное уведомление",
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
