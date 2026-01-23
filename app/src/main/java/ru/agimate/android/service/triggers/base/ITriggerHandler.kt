package ru.agimate.android.service.triggers.base

import ru.agimate.android.data.model.TriggerType

interface ITriggerHandler {
    val triggerType: TriggerType
    fun register()
    fun unregister()
}
