package ru.agimate.android.data.model

import androidx.annotation.StringRes
import ru.agimate.android.R

enum class ActionType(@StringRes val displayNameResId: Int, val actionName: String) {
    TTS(R.string.action_tts, "android.action.tts.speak"),
    NOTIFICATION(R.string.action_notification, "android.action.notification.show");

    companion object {
        private val byActionName = entries.associateBy { it.actionName }

        fun fromActionName(name: String): ActionType? = byActionName[name]
    }
}
