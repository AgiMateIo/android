package ru.agimate.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.agimate.android.domain.repository.ISettingsRepository
import ru.agimate.android.domain.usecase.TestConnectionUseCase
import ru.agimate.android.util.Logger

data class SettingsUiState(
    val apiKey: String = "",
    val serverUrl: String = "",
    val deviceId: String = "",
    val appVersion: String = "",
    val debugLogging: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: ConnectionTestResult? = null
)

sealed class ConnectionTestResult {
    data object Success : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}

class SettingsViewModel(
    private val settingsRepository: ISettingsRepository,
    private val testConnectionUseCase: TestConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    apiKey = settings.apiKey,
                    serverUrl = settings.serverUrl,
                    deviceId = settings.deviceId,
                    appVersion = settings.appVersion,
                    debugLogging = settings.debugLogging
                )
            }
        }
    }

    fun updateApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _uiState.value = _uiState.value.copy(apiKey = trimmed)
        viewModelScope.launch {
            settingsRepository.saveApiKey(trimmed)
            Logger.d("Device Key updated")
        }
    }

    fun updateServerUrl(serverUrl: String) {
        _uiState.value = _uiState.value.copy(serverUrl = serverUrl)
        viewModelScope.launch {
            settingsRepository.saveServerUrl(serverUrl)
            Logger.d("Server URL updated: $serverUrl")
        }
    }

    fun updateDebugLogging(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(debugLogging = enabled)
        viewModelScope.launch {
            settingsRepository.saveDebugLogging(enabled)
            Logger.d("Debug logging: $enabled")
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isTestingConnection = true,
                connectionTestResult = null
            )

            val result = testConnectionUseCase(
                apiKey = currentState.apiKey,
                serverUrl = currentState.serverUrl,
                deviceId = currentState.deviceId
            )

            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                connectionTestResult = if (result.isSuccess) {
                    ConnectionTestResult.Success
                } else {
                    ConnectionTestResult.Error(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun clearConnectionTestResult() {
        _uiState.value = _uiState.value.copy(connectionTestResult = null)
    }
}
