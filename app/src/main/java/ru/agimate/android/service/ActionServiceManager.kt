package ru.agimate.android.service

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.agimate.android.domain.repository.IActionStateRepository
import ru.agimate.android.util.Logger

class ActionServiceManager(
    private val context: Context,
    private val actionStateRepo: IActionStateRepository
) {

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    suspend fun updateServiceState() {
        val anyEnabled = actionStateRepo.hasAnyEnabled()

        if (anyEnabled && !_isServiceRunning.value) {
            startService()
        } else if (!anyEnabled && _isServiceRunning.value) {
            stopService()
        }
    }

    fun startService() {
        Logger.i("Starting ActionExecutorService")
        val intent = Intent(context, ActionExecutorService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isServiceRunning.value = true
    }

    fun stopService() {
        Logger.i("Stopping ActionExecutorService")
        val intent = Intent(context, ActionExecutorService::class.java)
        context.stopService(intent)
        _isServiceRunning.value = false
    }

    fun checkServiceState(): Boolean {
        return _isServiceRunning.value
    }
}
