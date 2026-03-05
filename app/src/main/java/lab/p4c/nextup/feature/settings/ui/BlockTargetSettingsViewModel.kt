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
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.settings.infra.InstalledAppFetcher
import lab.p4c.nextup.feature.settings.ui.model.BlockTargetItemUi
import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import java.time.Duration

data class BlockTargetSettingsUi(
    val items: List<BlockTargetItemUi> = emptyList(),
    val visibleItems: List<BlockTargetItemUi> = emptyList(),
    val initialSelected: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val query: String = "",
    val errorMessage: String? = null,
) {
    val currentSelected: Set<String>
        get() = items.filter { it.checked }.map { it.packageName }.toSet()

    val hasChanges: Boolean
        get() = currentSelected != initialSelected
}

@HiltViewModel
class BlockTargetSettingsViewModel @Inject constructor(
    private val repo: BlockTargetRepository,
    private val appFetcher: InstalledAppFetcher,
    private val usageStats: UsageStatsService,
    private val telemetryLogger: TelemetryLogger,
) : ViewModel() {

    private companion object {
        private const val USAGE_ROLLING_DAYS = 7L
    }

    private val _ui = MutableStateFlow(BlockTargetSettingsUi())
    val ui: StateFlow<BlockTargetSettingsUi> = _ui.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val blocked = repo.getTargets()
                    val apps = appFetcher.fetchInstalledApps()
                    val usageResult = usageStats.fetch(
                        range = Duration.ofDays(USAGE_ROLLING_DAYS)
                    )
                    Triple(blocked, apps, usageResult)
                }
            }

            result.onSuccess { (blocked, apps, usageResult) ->
                val usageMap = usageResult.summary.associate { row ->
                    row.packageName to row.total.toMillis()
                }

                val sortedItems = apps.map { app ->
                    BlockTargetItemUi(
                        packageName = app.packageName,
                        appName = app.appName,
                        icon = app.icon,
                        checked = blocked.contains(app.packageName),
                        usageMillis = usageMap[app.packageName] ?: 0L
                    )
                }.sortedByDescending { it.usageMillis }

                _ui.value = BlockTargetSettingsUi(
                    items = sortedItems,
                    visibleItems = filterItems(sortedItems, query = ""),
                    initialSelected = blocked,
                    isLoading = false,
                    query = "",
                    errorMessage = null,
                )
            }.onFailure { e ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        visibleItems = emptyList(),
                        errorMessage = "앱 목록을 불러오지 못했어요. (${e.javaClass.simpleName})"
                    )
                }
            }
        }
    }

    fun toggle(packageName: String) {
        _ui.update { prev ->
            val updatedItems = prev.items.map { item ->
                if (item.packageName == packageName) item.copy(checked = !item.checked)
                else item
            }
            prev.copy(
                items = updatedItems,
                visibleItems = filterItems(updatedItems, prev.query)
            )
        }
    }

    fun save() {
        val uiNow = _ui.value

        val selected = uiNow.items
            .filter { it.checked }
            .map { it.packageName }
            .toSet()

        val before = uiNow.initialSelected
        val added = (selected - before).toList().sorted()
        val removed = (before - selected).toList().sorted()

        if (added.isEmpty() && removed.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repo.setTargets(selected)
                }
            }.onSuccess {
                telemetryLogger.log(
                    eventName = "TargetAppChanged",
                    payload = mapOf(
                        "AddApps" to added.joinToString(","),
                        "SubApps" to removed.joinToString(",")
                    )
                )

                _ui.update { it.copy(initialSelected = selected) }
            }.onFailure { e ->
                _ui.update {
                    it.copy(errorMessage = "저장에 실패했어요. (${e.javaClass.simpleName})")
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _ui.update { prev ->
            prev.copy(
                query = query,
                visibleItems = filterItems(prev.items, query)
            )
        }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    private fun filterItems(
        items: List<BlockTargetItemUi>,
        query: String,
    ): List<BlockTargetItemUi> {
        val q = query.trim()
        if (q.isEmpty()) return items

        return items.filter { item ->
            item.appName.contains(q, ignoreCase = true) ||
                    item.packageName.contains(q, ignoreCase = true)
        }
    }
}