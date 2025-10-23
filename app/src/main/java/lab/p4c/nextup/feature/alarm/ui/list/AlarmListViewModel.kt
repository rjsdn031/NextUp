package lab.p4c.nextup.feature.alarm.ui.list

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
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.usecase.DeleteAlarmAndCancel
import lab.p4c.nextup.core.domain.alarm.usecase.ToggleAlarm
import lab.p4c.nextup.core.domain.alarm.usecase.UpsertAlarmAndReschedule
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    repo: AlarmRepository,
    private val toggleAlarm: ToggleAlarm,
    private val upsert: UpsertAlarmAndReschedule,
    private val delete: DeleteAlarmAndCancel,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> =
        repo.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _now = MutableStateFlow(timeProvider.nowLocal().atZone(ZoneId.systemDefault()))
    val now: StateFlow<ZonedDateTime> = _now


    init {
        viewModelScope.launch {
            while (isActive) {
                _now.value = timeProvider.nowLocal().atZone(ZoneId.systemDefault())
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