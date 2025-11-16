package lab.p4c.nextup.feature.usage.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import lab.p4c.nextup.feature.usage.ui.mapper.toUiRows
import lab.p4c.nextup.feature.usage.ui.mapper.toUiSessions
import lab.p4c.nextup.feature.usage.ui.model.AppUsageRow
import lab.p4c.nextup.feature.usage.ui.model.UsageSession

@HiltViewModel
class UsageStatsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UsageStatsUiState())
    val state: StateFlow<UsageStatsUiState> = _state

    fun load(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = UsageStatsService.fetch(context, range = Duration.ofHours(1))

            _state.update {
                it.copy(
                    isLoading = false,
                    rows = result.toUiRows(),
                    sessionsByApp = result.toUiSessions(),
                    error = result.error
                )
            }
        }
    }
}

data class UsageStatsUiState(
    val isLoading: Boolean = false,
    val rows: List<AppUsageRow> = emptyList(),
    val sessionsByApp: Map<String, List<UsageSession>> = emptyMap(),
    val error: String? = null
)
