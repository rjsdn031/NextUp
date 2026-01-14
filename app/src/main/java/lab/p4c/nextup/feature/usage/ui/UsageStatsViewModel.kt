package lab.p4c.nextup.feature.usage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository
import lab.p4c.nextup.feature.usage.data.repository.UsageSessionInput
import java.time.Duration
import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import lab.p4c.nextup.feature.usage.ui.mapper.toUiRows
import lab.p4c.nextup.feature.usage.ui.mapper.toUiSessions
import lab.p4c.nextup.feature.usage.ui.model.AppUsageRow
import lab.p4c.nextup.feature.usage.ui.model.UsageSession

@HiltViewModel
class UsageStatsViewModel @Inject constructor(
    private val usageStatsService: UsageStatsService,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsageStatsUiState())
    val state: StateFlow<UsageStatsUiState> = _state

    private var lastPersistMs: Long = 0L
    private val persistCooldownMs = 10 * 60_000L

    fun refreshOnResume() {
        viewModelScope.launch {
            val granted = usageStatsService.hasPermission()
            _state.update { it.copy(hasPermission = granted) }

            if (granted) load(Duration.ofHours(24))
            else _state.update { it.copy(isLoading = false, dataReady = false) }
        }
    }

    fun openUsageAccessSettings() {
        usageStatsService.requestPermission()
    }

    fun load(duration: Duration) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val endMs = System.currentTimeMillis()
            val startMs = endMs - duration.toMillis()

            val result = usageStatsService.fetch(range = duration)

            _state.update {
                it.copy(
                    isLoading = false,
                    rows = result.toUiRows(),
                    sessionsByApp = result.toUiSessions(),
                    error = result.error,
                    windowStartMs = startMs,
                    windowEndMs = endMs
                )
            }

            if (result.error == null) {
                val now = System.currentTimeMillis()
                val shouldPersist = now - lastPersistMs > persistCooldownMs
                if (!shouldPersist) return@launch

                val inputs = result.sessionsByApp.values.flatten().map { s ->
                    UsageSessionInput(
                        packageName = s.packageName,
                        startMillis = s.startMillis,
                        endMillis = s.endMillis
                    )
                }

                runCatching {
                    usageRepository.saveSessions(inputs)
                }.onSuccess {
                    lastPersistMs = now
                }
            }
        }
    }
}

data class UsageStatsUiState(
    val hasPermission: Boolean = false,
    val dataReady: Boolean = true,
    val isLoading: Boolean = false,
    val rows: List<AppUsageRow> = emptyList(),
    val sessionsByApp: Map<String, List<UsageSession>> = emptyMap(),
    val error: String? = null,
    val windowStartMs: Long = 0L,
    val windowEndMs: Long = 0L
)
