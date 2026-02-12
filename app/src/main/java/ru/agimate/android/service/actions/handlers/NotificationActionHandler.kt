package ru.agimate.android.service.actions.handlers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.serialization.json.jsonPrimitive
import ru.agimate.android.R
import ru.agimate.android.data.model.ActionTask
import ru.agimate.android.data.model.ActionType
import ru.agimate.android.service.actions.base.IActionHandler
import ru.agimate.android.util.Logger

class NotificationActionHandler(
    private val context: Context
) : IActionHandler {

    override val actionType = ActionType.NOTIFICATION

    private var notificationManager: NotificationManager? = null
    private var notificationId = 2000

    override fun initialize() {
        Logger.i("Initializing Notification handler")
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_action_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_action_channel_desc)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override suspend fun execute(task: ActionTask) {
        val title = task.parameters["title"]?.jsonPrimitive?.content
            ?: context.getString(R.string.notification_default_title)
        val message = task.parameters["message"]?.jsonPrimitive?.content

        if (message == null) {
            Logger.e("Notification task missing 'message' parameter")
            return
        }

        Logger.i("Showing notification: title=$title, message=$message")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(notificationId++, notification)
    }

    override fun cleanup() {
        Logger.i("Cleaning up Notification handler")
        notificationManager = null
    }

    companion object {
        const val CHANNEL_ID = "action_notifications_channel"
    }
}
