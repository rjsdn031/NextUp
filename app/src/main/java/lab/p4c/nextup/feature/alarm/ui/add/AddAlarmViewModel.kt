package lab.p4c.nextup.feature.alarm.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.usecase.UpsertAlarmAndReschedule
import lab.p4c.nextup.util.getTimeUntilAlarm
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

data class AddAlarmUiState(
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: List<Int> = emptyList(),     // 1=월..7=일
    val skipHolidays: Boolean = true,

    val alarmSoundEnabled: Boolean = true,
    val ringtoneName: String = "Classic Bell",
    val ringtonePath: String = "assets/sounds/test_sound.mp3",

    val vibration: Boolean = true,
    val volume: Float = 1f,                      // 0..1
    val fadeSeconds: Int = 0,
    val loop: Boolean = true,

    val snoozeEnabled: Boolean = false,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = 3,

    val isPreviewing: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val nextTriggerText: String? = null
) {
    val canSave: Boolean =
        !isBusy && hour in 0..23 && minute in 0..59 && label.length <= 30
}

@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    private val upsert: UpsertAlarmAndReschedule
) : ViewModel() {

    private val _ui = MutableStateFlow(AddAlarmUiState())
    val ui = _ui.asStateFlow()

    /* ---------- 업데이트 ---------- */
    fun updateTime(h: Int, m: Int) {
        _ui.value = _ui.value.copy(hour = h, minute = m)
        recalcNextTrigger()
    }
    fun updateLabel(s: String) { _ui.value = _ui.value.copy(label = s) }
    fun updateDays(days: List<Int>) { _ui.value = _ui.value.copy(repeatDays = days); recalcNextTrigger() }
    fun toggleSkipHolidays(b: Boolean) { _ui.value = _ui.value.copy(skipHolidays = b); recalcNextTrigger() }

    fun toggleAlarmSound(enabled: Boolean) {
        val s = _ui.value
        _ui.value = s.copy(alarmSoundEnabled = enabled, isPreviewing = if (!enabled) false else s.isPreviewing)
    }
    fun selectSound(name: String, path: String) { _ui.value = _ui.value.copy(ringtoneName = name, ringtonePath = path) }
    fun toggleVibration(b: Boolean) { _ui.value = _ui.value.copy(vibration = b) }
    fun updateVolume(v: Float) { _ui.value = _ui.value.copy(volume = v.coerceIn(0f, 1f)) }
    fun toggleFade(on: Boolean) { _ui.value = _ui.value.copy(fadeSeconds = if (on) 30 else 0) }
    fun toggleLoop(b: Boolean) { _ui.value = _ui.value.copy(loop = b) }
    fun toggleSnoozeEnabled(b: Boolean) { _ui.value = _ui.value.copy(snoozeEnabled = b) }
    fun selectSnooze(interval: Int, count: Int) {
        _ui.value = _ui.value.copy(snoozeInterval = interval, maxSnoozeCount = count)
    }

    fun togglePreview() {
        val s = _ui.value
        if (!s.alarmSoundEnabled || s.ringtonePath.isBlank()) return
        _ui.value = s.copy(isPreviewing = !s.isPreviewing)
    }
    fun consumeError() { _ui.value = _ui.value.copy(errorMessage = null) }

    /* ---------- 저장 ---------- */
    fun save(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val s = _ui.value
        if (!s.canSave) return@launch
        _ui.value = s.copy(isBusy = true)
        try {
            val alarm = Alarm(
                id = 0, // 신규
                hour = s.hour,
                minute = s.minute,
                days = intsToDays(s.repeatDays),
                skipHolidays = s.skipHolidays,
                enabled = true,

                assetAudioPath = s.ringtonePath,
                alarmSoundEnabled = s.alarmSoundEnabled,
                ringtoneName = s.ringtoneName,
                volume = s.volume.toDouble(),
                fadeDuration = s.fadeSeconds,
                name = s.label,
                notificationBody = "기상 시간입니다.",
                loopAudio = s.loop,
                vibration = s.vibration,
                warningNotificationOnKill = true,
                androidFullScreenIntent = true,
                snoozeEnabled = s.snoozeEnabled,
                snoozeInterval = s.snoozeInterval,
                maxSnoozeCount = s.maxSnoozeCount
            )
            upsert(alarm)
            onDone(true)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(errorMessage = "알람 저장 실패: ${e.message}")
            onDone(false)
        } finally {
            _ui.value = _ui.value.copy(isBusy = false)
        }
    }

    /* ---------- 내부 유틸 ---------- */
    private fun intsToDays(days: List<Int>): Set<DayOfWeek> = days.mapNotNull {
        when (it) {
            1 -> DayOfWeek.MONDAY
            2 -> DayOfWeek.TUESDAY
            3 -> DayOfWeek.WEDNESDAY
            4 -> DayOfWeek.THURSDAY
            5 -> DayOfWeek.FRIDAY
            6 -> DayOfWeek.SATURDAY
            7 -> DayOfWeek.SUNDAY
            else -> null
        }
    }.toSet()

    private fun recalcNextTrigger() {
        val s = _ui.value
        val line = getTimeUntilAlarm(s.hour, s.minute, ZonedDateTime.now(ZoneId.systemDefault()))
        _ui.value = s.copy(nextTriggerText = line)
    }
}
