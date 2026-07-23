package com.example.mindquest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindquest.data.repository.QuizRepository
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.settings.Difficulty
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.data.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isParentGateVisible: Boolean = false,
    val gateA: Int = 0,
    val gateB: Int = 0,
    val gateAnswer: String = "",
    val gateError: Boolean = false,
    val dataClearedMessage: Boolean = false
)

/**
 * The parental gate only guards the destructive "clear my data" action, not every preference
 * toggle — gating difficulty/sound too would just add friction for a child using the app
 * unsupervised, without any real privacy benefit. See README ethics-to-design table.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val quizRepository: QuizRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch { settingsRepository.setDifficulty(difficulty) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun requestClearData() {
        _uiState.value = _uiState.value.copy(
            isParentGateVisible = true,
            gateA = (1..9).random(),
            gateB = (1..9).random(),
            gateAnswer = "",
            gateError = false
        )
    }

    fun updateGateAnswer(text: String) {
        _uiState.value = _uiState.value.copy(gateAnswer = text, gateError = false)
    }

    fun submitGateAnswer() {
        val state = _uiState.value
        if (state.gateAnswer.toIntOrNull() == state.gateA + state.gateB) {
            viewModelScope.launch {
                settingsRepository.clearAll()
                quizRepository.clearCache()
                statsRepository.clearHistory()
                _uiState.value = _uiState.value.copy(
                    isParentGateVisible = false,
                    dataClearedMessage = true
                )
            }
        } else {
            _uiState.value = state.copy(gateError = true)
        }
    }

    fun dismissParentGate() {
        _uiState.value = _uiState.value.copy(isParentGateVisible = false)
    }

    fun dismissDataClearedMessage() {
        _uiState.value = _uiState.value.copy(dataClearedMessage = false)
    }
}
