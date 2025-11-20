package lab.p4c.nextup.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import lab.p4c.nextup.feature.settings.infra.InstalledAppFetcher
import lab.p4c.nextup.feature.settings.ui.model.BlockTargetItemUi
import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

data class BlockTargetSettingsUi(
    val items: List<BlockTargetItemUi> = emptyList(),
    val initialSelected: Set<String> = emptySet(),
    val isLoading: Boolean = true
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
) : ViewModel() {

    private val _ui = MutableStateFlow(BlockTargetSettingsUi())
    val ui: StateFlow<BlockTargetSettingsUi> = _ui.asStateFlow()

    init {
        loadApps()
    }

    /**
     * 설치된 앱 목록 + 저장된 차단 목록 + usageMillis(24h) 결합
     */
    private fun loadApps() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }

            // 1) 저장된 차단 목록
            val blocked = repo.getTargets()

            // 2) 설치 앱 목록
            val apps = appFetcher.fetchInstalledApps()

            // 3) 최근 24시간 usage 데이터
            val usageResult = withContext(Dispatchers.IO) {
                usageStats.fetch(
                    context = appFetcher.context,
                    range = Duration.ofHours(24)
                )
            }

            // 사용량 map: packageName -> millis
            val usageMap = usageResult.summary.associate { row ->
                row.packageName to row.total.toMillis()
            }

            // 4) UI 모델 생성
            val items = apps.map { app ->
                BlockTargetItemUi(
                    packageName = app.packageName,
                    appName = app.appName,
                    icon = app.icon,
                    checked = blocked.contains(app.packageName),
                    usageMillis = usageMap[app.packageName] ?: 0L
                )
            }

            // 5) 사용량 기준 정렬
            val sorted = items.sortedByDescending { it.usageMillis }

            _ui.value = BlockTargetSettingsUi(
                items = sorted,
                initialSelected = blocked,
                isLoading = false
            )
        }
    }

    /**
     * 항목 토글
     */
    fun toggle(packageName: String) {
        val updated = _ui.value.items.map {
            if (it.packageName == packageName) it.copy(checked = !it.checked)
            else it
        }
        _ui.update { it.copy(items = updated) }
    }

    /**
     * 현재 체크된 항목 저장
     */
    fun save() {
        val selected = _ui.value.items
            .filter { it.checked }
            .map { it.packageName }
            .toSet()

        viewModelScope.launch {
            repo.setTargets(selected)
        }
    }
}
