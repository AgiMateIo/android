package ru.agimate.android.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ru.agimate.android.data.model.TriggerType

class PermissionHelper(private val context: Context) {

    companion object {
        fun hasPermissions(context: Context, permissions: List<String>): Boolean {
            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun hasPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequiredPermissionsForTrigger(triggerType: TriggerType): List<String> {
        return when (triggerType) {
            TriggerType.INCOMING_CALL -> listOf(
                Manifest.permission.READ_PHONE_STATE
            )
            TriggerType.WIFI -> listOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            TriggerType.BATTERY_LOW, TriggerType.SHAKE -> emptyList()
        }
    }

    fun hasPermissionsForTrigger(triggerType: TriggerType): Boolean {
        val required = getRequiredPermissionsForTrigger(triggerType)
        return required.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }
}
