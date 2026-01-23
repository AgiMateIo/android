package ru.agimate.android.service.actions.base

import ru.agimate.android.data.model.ActionTask
import ru.agimate.android.data.model.ActionType

interface IActionHandler {
    val actionType: ActionType
    suspend fun execute(task: ActionTask)
    fun initialize()
    fun cleanup()
}
