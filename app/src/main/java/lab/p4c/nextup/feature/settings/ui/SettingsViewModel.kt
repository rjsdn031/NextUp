package lab.p4c.nextup.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRepository

/**
 * Exposes summary state needed by [AlarmSettingsScreen].
 *
 * This view model keeps the settings screen independent from experiment-info
 * persistence details and only provides a small UI-ready projection.
 *
 * @property experimentInfoRepository Repository that reads the saved experiment info.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val experimentInfoRepository: ExperimentInfoRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        refreshExperimentInfo()
    }

    /**
     * Reloads the saved experiment info and updates the settings summary state.
     */
    fun refreshExperimentInfo() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    experimentInfoRepository.get()
                }
            }.onSuccess { info ->
                _ui.update { current ->
                    current.copy(
                        experimentInfoEntry = info.toExperimentInfoEntryUi()
                    )
                }
            }.onFailure {
                _ui.update { current ->
                    current.copy(
                        experimentInfoEntry = ExperimentInfoEntryUiState()
                    )
                }
            }
        }
    }
}

/**
 * Aggregated UI state for the settings screen.
 *
 * @property experimentInfoEntry Summary state for the experiment-info entry card.
 */
data class SettingsUiState(
    val experimentInfoEntry: ExperimentInfoEntryUiState = ExperimentInfoEntryUiState(),
)

/**
 * UI-ready summary for the experiment-info entry card.
 *
 * @property isCompleted Whether the experiment info is already filled enough to edit.
 * @property buttonText CTA text shown on the trailing button.
 * @property supportingText Supporting description under the title.
 */
data class ExperimentInfoEntryUiState(
    val isCompleted: Boolean = false,
    val buttonText: String = "입력",
    val supportingText: String = "실험자 이름, 나이, 성별을 입력하세요",
)

private fun ExperimentInfo?.toExperimentInfoEntryUi(): ExperimentInfoEntryUiState {
    val isCompleted = this != null &&
            name.isNotBlank()

    return if (isCompleted) {
        ExperimentInfoEntryUiState(
            isCompleted = true,
            buttonText = "수정",
            supportingText = "입력한 실험자 정보를 수정할 수 있습니다",
        )
    } else {
        ExperimentInfoEntryUiState()
    }
}