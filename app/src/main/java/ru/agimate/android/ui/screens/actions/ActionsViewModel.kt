package ru.agimate.android.ui.screens.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.agimate.android.data.model.ActionDefinition
import ru.agimate.android.data.model.ActionType
import ru.agimate.android.domain.repository.IActionStateRepository
import ru.agimate.android.service.ActionServiceManager
import ru.agimate.android.util.Logger

data class ActionsUiState(
    val actions: List<ActionUiItem> = emptyList(),
    val isServiceRunning: Boolean = false,
    val permissionRequest: PermissionRequest? = null
)

data class ActionUiItem(
    val definition: ActionDefinition,
    val isEnabled: Boolean,
    val hasPermissions: Boolean
)

data class PermissionRequest(
    val actionType: ActionType,
    val permissions: List<String>
)

class ActionsViewModel(
    private val actionStateRepository: IActionStateRepository,
    private val actionServiceManager: ActionServiceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActionsUiState())
    val uiState: StateFlow<ActionsUiState> = _uiState.asStateFlow()

    private val _permissionsGranted = MutableStateFlow<Map<ActionType, Boolean>>(emptyMap())

    init {
        observeActionsAndService()
    }

    private fun observeActionsAndService() {
        viewModelScope.launch {
            combine(
                actionStateRepository.getAllStates(),
                actionServiceManager.isServiceRunning,
                _permissionsGranted
            ) { states, isRunning, permissions ->
                val actions = ActionDefinition.ALL_ACTIONS.map { definition ->
                    ActionUiItem(
                        definition = definition,
                        isEnabled = states[definition.type] ?: false,
                        hasPermissions = permissions[definition.type] ?: definition.requiredPermissions.isEmpty()
                    )
                }
                Triple(actions, isRunning, Unit)
            }.collect { (actions, isRunning, _) ->
                _uiState.value = _uiState.value.copy(
                    actions = actions,
                    isServiceRunning = isRunning
                )
            }
        }
    }

    fun updatePermissionStatus(actionType: ActionType, granted: Boolean) {
        _permissionsGranted.value = _permissionsGranted.value + (actionType to granted)
    }

    fun toggleAction(actionType: ActionType, enabled: Boolean) {
        val action = ActionDefinition.ALL_ACTIONS.find { it.type == actionType } ?: return

        // Check if permissions are needed
        if (enabled && action.requiredPermissions.isNotEmpty()) {
            val hasPermissions = _permissionsGranted.value[actionType] ?: false
            if (!hasPermissions) {
                _uiState.value = _uiState.value.copy(
                    permissionRequest = PermissionRequest(
                        actionType = actionType,
                        permissions = action.requiredPermissions
                    )
                )
                return
            }
        }

        viewModelScope.launch {
            Logger.i("Toggling action $actionType to $enabled")
            actionStateRepository.setEnabled(actionType, enabled)
            actionServiceManager.updateServiceState()
        }
    }

    fun onPermissionsResult(granted: Boolean) {
        val request = _uiState.value.permissionRequest ?: return
        _uiState.value = _uiState.value.copy(permissionRequest = null)

        updatePermissionStatus(request.actionType, granted)

        if (granted) {
            toggleAction(request.actionType, true)
        }
    }

    fun clearPermissionRequest() {
        _uiState.value = _uiState.value.copy(permissionRequest = null)
    }
}
