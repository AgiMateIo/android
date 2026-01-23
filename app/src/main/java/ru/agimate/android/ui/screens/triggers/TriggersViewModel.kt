package ru.agimate.android.ui.screens.triggers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.agimate.android.data.model.TriggerDefinition
import ru.agimate.android.data.model.TriggerEvent
import ru.agimate.android.data.model.TriggerType
import ru.agimate.android.domain.repository.ITriggerEventRepository
import ru.agimate.android.domain.repository.ITriggerStateRepository
import ru.agimate.android.service.TriggerServiceManager
import ru.agimate.android.util.Logger

data class TriggersUiState(
    val triggers: List<TriggerUiItem> = emptyList(),
    val recentEvents: List<TriggerEvent> = emptyList(),
    val isServiceRunning: Boolean = false,
    val permissionRequest: PermissionRequest? = null
)

data class TriggerUiItem(
    val definition: TriggerDefinition,
    val isEnabled: Boolean,
    val hasPermissions: Boolean
)

data class PermissionRequest(
    val triggerType: TriggerType,
    val permissions: List<String>
)

class TriggersViewModel(
    private val triggerStateRepository: ITriggerStateRepository,
    private val triggerEventRepository: ITriggerEventRepository,
    private val triggerServiceManager: TriggerServiceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriggersUiState())
    val uiState: StateFlow<TriggersUiState> = _uiState.asStateFlow()

    private val _permissionsGranted = MutableStateFlow<Map<TriggerType, Boolean>>(emptyMap())

    init {
        observeTriggersAndEvents()
        observeServiceState()
    }

    private fun observeTriggersAndEvents() {
        viewModelScope.launch {
            combine(
                triggerStateRepository.getAllStates(),
                triggerEventRepository.getRecentEvents(),
                _permissionsGranted
            ) { states, events, permissions ->
                val triggers = TriggerDefinition.ALL_TRIGGERS.map { definition ->
                    TriggerUiItem(
                        definition = definition,
                        isEnabled = states[definition.type] ?: false,
                        hasPermissions = permissions[definition.type] ?: definition.requiredPermissions.isEmpty()
                    )
                }
                Triple(triggers, events, Unit)
            }.collect { (triggers, events, _) ->
                _uiState.value = _uiState.value.copy(
                    triggers = triggers,
                    recentEvents = events
                )
            }
        }
    }

    private fun observeServiceState() {
        viewModelScope.launch {
            triggerServiceManager.isServiceRunning.collect { isRunning ->
                _uiState.value = _uiState.value.copy(isServiceRunning = isRunning)
            }
        }
    }

    fun updatePermissionStatus(triggerType: TriggerType, granted: Boolean) {
        _permissionsGranted.value = _permissionsGranted.value + (triggerType to granted)
    }

    fun toggleTrigger(triggerType: TriggerType, enabled: Boolean) {
        val trigger = TriggerDefinition.ALL_TRIGGERS.find { it.type == triggerType } ?: return

        // Check if permissions are needed
        if (enabled && trigger.requiredPermissions.isNotEmpty()) {
            val hasPermissions = _permissionsGranted.value[triggerType] ?: false
            if (!hasPermissions) {
                _uiState.value = _uiState.value.copy(
                    permissionRequest = PermissionRequest(
                        triggerType = triggerType,
                        permissions = trigger.requiredPermissions
                    )
                )
                return
            }
        }

        viewModelScope.launch {
            Logger.i("Toggling trigger $triggerType to $enabled")
            triggerStateRepository.setEnabled(triggerType, enabled)
            triggerServiceManager.updateServiceState()
        }
    }

    fun onPermissionsResult(granted: Boolean) {
        val request = _uiState.value.permissionRequest ?: return
        _uiState.value = _uiState.value.copy(permissionRequest = null)

        updatePermissionStatus(request.triggerType, granted)

        if (granted) {
            toggleTrigger(request.triggerType, true)
        }
    }

    fun clearPermissionRequest() {
        _uiState.value = _uiState.value.copy(permissionRequest = null)
    }

    fun clearEventHistory() {
        viewModelScope.launch {
            triggerEventRepository.clearHistory()
        }
    }
}
