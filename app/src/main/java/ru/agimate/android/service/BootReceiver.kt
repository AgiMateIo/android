package ru.agimate.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.agimate.android.util.Logger
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i("Boot completed, scheduling trigger service start...")

            // Use WorkManager to start the service after a short delay
            // This avoids ForegroundServiceStartNotAllowedException on Android 12+
            val workRequest = OneTimeWorkRequestBuilder<StartTriggerServiceWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            Logger.i("Trigger service start scheduled via WorkManager")
        }
    }
}