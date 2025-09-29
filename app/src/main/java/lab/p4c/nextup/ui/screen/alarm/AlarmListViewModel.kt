package lab.p4c.nextup.ui.screen.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.domain.repository.AlarmRepository
import lab.p4c.nextup.domain.usecase.DeleteAlarmAndCancel
import lab.p4c.nextup.domain.usecase.ToggleAlarm
import lab.p4c.nextup.domain.usecase.UpsertAlarmAndReschedule
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    repo: AlarmRepository,
    private val toggleAlarm: ToggleAlarm,
    private val upsert: UpsertAlarmAndReschedule,
    private val delete: DeleteAlarmAndCancel
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> =
        repo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _now = MutableStateFlow(Instant.now())
    val now: StateFlow<Instant> = _now

    init {
        viewModelScope.launch {
            while (isActive) {
                _now.value = Instant.now()
                delay(1_000)
            }
        }
    }

    fun onToggle(alarm: Alarm, enabled: Boolean) = viewModelScope.launch {
        toggleAlarm(alarm.id, enabled)
    }

    fun onDelete(alarmId: Int) = viewModelScope.launch {
        delete(alarmId)
    }

    fun onUpsert(alarm: Alarm) = viewModelScope.launch {
        upsert(alarm)
    }
}
