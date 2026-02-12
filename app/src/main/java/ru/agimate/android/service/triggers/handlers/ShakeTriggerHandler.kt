package ru.agimate.android.service.triggers.handlers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.service.triggers.base.BaseTriggerHandler
import ru.agimate.android.util.Logger
import kotlin.math.sqrt

class ShakeTriggerHandler(
    context: Context,
    sendTriggerUseCase: SendTriggerUseCase
) : BaseTriggerHandler(context, sendTriggerUseCase), SensorEventListener {

    override val triggerType = TriggerType.SHAKE

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isRegistered = false
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastShakeTime: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD = 2500
        private const val UPDATE_INTERVAL_MS = 100
        private const val SHAKE_COOLDOWN_MS = 1000
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        if ((currentTime - lastUpdate) > UPDATE_INTERVAL_MS) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val acceleration = sqrt(
                deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
            ) / deltaTime * 10000

            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > SHAKE_COOLDOWN_MS) {
                lastShakeTime = currentTime
                Logger.i("Shake detected! Acceleration: $acceleration")

                val data = buildJsonObject {
                    put("acceleration", acceleration.toDouble())
                    put("x", deltaX.toDouble())
                    put("y", deltaY.toDouble())
                    put("z", deltaZ.toDouble())
                    put("timestamp", currentTime)
                }

                sendEvent(
                    name = "android.trigger.shake.detected",
                    data = data
                )
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun register() {
        if (!isRegistered && accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            isRegistered = true
            Logger.i("ShakeTriggerHandler registered")
        }
    }

    override fun unregister() {
        if (isRegistered) {
            sensorManager.unregisterListener(this)
            isRegistered = false
            Logger.i("ShakeTriggerHandler unregistered")
        }
    }
}
