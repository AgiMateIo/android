package ru.agimate.android.service

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.agimate.android.domain.repository.ITriggerStateRepository
import ru.agimate.android.util.Logger

class TriggerServiceManager(
    private val context: Context,
    private val triggerStateRepo: ITriggerStateRepository
) {

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    suspend fun updateServiceState() {
        val anyEnabled = triggerStateRepo.hasAnyEnabled()

        if (anyEnabled && !_isServiceRunning.value) {
            startService()
        } else if (!anyEnabled && _isServiceRunning.value) {
            stopService()
        }
    }

    fun startService() {
        Logger.i("Starting TriggerMonitorService")
        val intent = Intent(context, TriggerMonitorService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isServiceRunning.value = true
    }

    fun stopService() {
        Logger.i("Stopping TriggerMonitorService")
        val intent = Intent(context, TriggerMonitorService::class.java)
        context.stopService(intent)
        _isServiceRunning.value = false
    }

    fun checkServiceState(): Boolean {
        return _isServiceRunning.value
    }
}
