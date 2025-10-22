package lab.p4c.nextup.feature.usage.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class UsageStatsSharedViewModel @Inject constructor() : ViewModel() {
    private val _sessionsByApp = MutableStateFlow<Map<String, List<UsageSession>>>(emptyMap())
    val sessionsByApp: StateFlow<Map<String, List<UsageSession>>> = _sessionsByApp

    fun setSessions(map: Map<String, List<UsageSession>>) {
        _sessionsByApp.value = map
    }

    fun getSessions(packageName: String): List<UsageSession> =
        _sessionsByApp.value[packageName].orEmpty()
}
