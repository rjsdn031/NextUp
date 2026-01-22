package lab.p4c.nextup.feature.settings.ui.experiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.auth.usecase.EnsureAnonymousSignedIn
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRepository
import lab.p4c.nextup.core.domain.experiment.usecase.UpsertExperimentInfoRemote

data class ExperimentInfoUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ExperimentInfoViewModel @Inject constructor(
    private val repo: ExperimentInfoRepository,
    private val ensureAnonymousSignedIn: EnsureAnonymousSignedIn,
    private val upsertExperimentInfoRemote: UpsertExperimentInfoRemote
) : ViewModel() {

    private val _info = MutableStateFlow<ExperimentInfo?>(null)
    val info: StateFlow<ExperimentInfo?> = _info

    private val _uiState = MutableStateFlow(ExperimentInfoUiState())
    val uiState: StateFlow<ExperimentInfoUiState> = _uiState

    init {
        viewModelScope.launch {
            _info.value = repo.get()
        }
    }

    fun save(name: String, age: String, gender: String) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                // --- Validation ---
                val trimmedName = name.trim()
                require(trimmedName.isNotEmpty())

                val parsedAge = age.toIntOrNull()
                require(parsedAge != null)

                require(gender.isNotBlank())

                val info = ExperimentInfo(
                    name = trimmedName,
                    age = parsedAge,
                    gender = gender
                )

                repo.save(info)
                _info.value = info

                val uid = ensureAnonymousSignedIn()
                upsertExperimentInfoRemote(uid, info)
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "저장에 실패했어요.") }
            }

            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
