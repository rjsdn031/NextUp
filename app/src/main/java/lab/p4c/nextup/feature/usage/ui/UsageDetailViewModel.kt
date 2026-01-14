package lab.p4c.nextup.feature.usage.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository
import lab.p4c.nextup.feature.usage.ui.model.UsageSession

@HiltViewModel
class UsageDetailViewModel @Inject constructor(
    private val repo: UsageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pkg: String? = savedStateHandle.get<String>("pkg")
    private val startMs: Long? = savedStateHandle.get<String>("startMs")?.toLongOrNull()
    private val endMs: Long? = savedStateHandle.get<String>("endMs")?.toLongOrNull()

    private val _state = MutableStateFlow(UsageDetailUiState())
    val state: StateFlow<UsageDetailUiState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val p = pkg
            val s = startMs
            val e = endMs

            if (p.isNullOrBlank() || s == null || e == null || e <= s) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        sessions = emptyList(),
                        error = "잘못된 접근입니다."
                    )
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repo.getSessionsByWindow(
                    packageName = p,
                    startMs = s,
                    endMs = e
                )
            }.onSuccess { list ->
                _state.update { it.copy(isLoading = false, sessions = list) }
            }.onFailure { t ->
                _state.update { it.copy(isLoading = false, error = t.message ?: "불러오기 실패") }
            }
        }
    }
}

data class UsageDetailUiState(
    val isLoading: Boolean = false,
    val sessions: List<UsageSession> = emptyList(),
    val error: String? = null
)
