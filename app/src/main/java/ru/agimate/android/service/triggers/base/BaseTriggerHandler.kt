package ru.agimate.android.service.triggers.base

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.util.Logger

abstract class BaseTriggerHandler(
    protected val context: Context,
    private val sendTriggerUseCase: SendTriggerUseCase
) : ITriggerHandler {

    protected val scope = CoroutineScope(Dispatchers.IO)

    protected fun sendEvent(
        name: String,
        type: String = "DEVICE_EVENT",
        data: JsonObject
    ) {
        scope.launch {
            try {
                val result = sendTriggerUseCase.execute(
                    type = type,
                    name = name,
                    data = data
                )
                if (result.isSuccess) {
                    Logger.i("Trigger sent: $name")
                } else {
                    Logger.e("Failed to send trigger: $name - ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Logger.e("Error sending trigger: $name", e)
            }
        }
    }
}
