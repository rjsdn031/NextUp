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
    private val appFetcher: InstalledAppFetcher
) : ViewModel() {

    private val _ui = MutableStateFlow(BlockTargetSettingsUi())
    val ui: StateFlow<BlockTargetSettingsUi> = _ui.asStateFlow()

    init {
        loadApps()
    }

    /**
     * 설치된 앱 목록 + 저장된 차단 목록을 결합해서 UI 상태 생성
     */
    private fun loadApps() {
        viewModelScope.launch {
            val blocked = repo.getTargets()
            val apps = appFetcher.fetchInstalledApps()

            val items = apps.map { app ->
                BlockTargetItemUi(
                    packageName = app.packageName,
                    appName = app.appName,
                    icon = app.icon,
                    checked = blocked.contains(app.packageName)
                )
            }

            _ui.value = BlockTargetSettingsUi(
                items = items,
                initialSelected = blocked,
                isLoading = false
            )
        }
    }

    /**
     * 패키지명을 기반으로 toggle
     */
    fun toggle(packageName: String) {
        val updated = _ui.value.items.map {
            if (it.packageName == packageName) {
                it.copy(checked = !it.checked)
            } else it
        }
        _ui.value = _ui.value.copy(items = updated)
    }

    /**
     * 현재 체크된 항목을 저장
     */
    fun save() {
        val selectedPackages = _ui.value.items
            .filter { it.checked }
            .map { it.packageName }
            .toSet()

        viewModelScope.launch {
            repo.setTargets(selectedPackages)
        }
    }
}
