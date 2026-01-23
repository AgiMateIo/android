package ru.agimate.android.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.agimate.android.util.Logger

class StartTriggerServiceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val triggerServiceManager: TriggerServiceManager by inject()

    override suspend fun doWork(): Result {
        Logger.i("StartTriggerServiceWorker: checking for enabled triggers...")
        return try {
            triggerServiceManager.updateServiceState()
            Logger.i("StartTriggerServiceWorker: completed successfully")
            Result.success()
        } catch (e: Exception) {
            Logger.e("StartTriggerServiceWorker: failed", e)
            Result.failure()
        }
    }
}