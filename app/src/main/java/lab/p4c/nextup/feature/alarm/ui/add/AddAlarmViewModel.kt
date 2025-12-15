package lab.p4c.nextup.feature.alarm.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.alarm.ui.util.NextTriggerFormatter
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.usecase.UpsertAlarmAndReschedule
import lab.p4c.nextup.core.common.time.indicesToDays
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import java.time.ZoneId

data class AddAlarmUiState(
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7),     // 1=월..7=일
    val skipHolidays: Boolean = false,

    val alarmSoundEnabled: Boolean = true,
    val ringtoneName: String = "기본 알람",
    val sound: AlarmSound = AlarmSound.Asset("test_sound"),

    val vibration: Boolean = true,
    val volume: Float = 0.8f,                      // 0..1
    val fadeSeconds: Int = 0,
    val loop: Boolean = true,

    val snoozeEnabled: Boolean = false,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = 3,

    val isPreviewing: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val nextTriggerText: String? = null,

    val isFirstAlarm: Boolean = false
) {
    val canSave: Boolean =
        !isBusy && hour in 0..23 && minute in 0..59 && label.length <= 30
}

sealed interface AddAlarmEvent {
    data object Saved : AddAlarmEvent
    data class SaveFailed(val message: String) : AddAlarmEvent
}

@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    private val repo: AlarmRepository,
    private val upsert: UpsertAlarmAndReschedule,
    private val timeProvider: TimeProvider,
    private val nextTrigger: NextTriggerCalculator,
) : ViewModel() {

    private val _ui = MutableStateFlow(AddAlarmUiState())
    val ui = _ui.asStateFlow()

    private val _events = MutableSharedFlow<AddAlarmEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val existing = repo.getAll()
            val isFirst = existing.isEmpty()

            _ui.value = _ui.value.copy(isFirstAlarm = isFirst)

            recalcNextTrigger()
        }
    }

    /* ---------- Update ---------- */
    fun updateTime(h: Int, m: Int) {
        _ui.value = _ui.value.copy(hour = h, minute = m)
        recalcNextTrigger()
    }

    fun updateLabel(s: String) {
        _ui.value = _ui.value.copy(label = s)
    }

    fun updateDays(days: List<Int>) {
        _ui.value = _ui.value.copy(repeatDays = days); recalcNextTrigger()
    }

    fun toggleSkipHolidays(b: Boolean) {
        _ui.value = _ui.value.copy(skipHolidays = b); recalcNextTrigger()
    }

    fun toggleAlarmSound(enabled: Boolean): Boolean {
        if (isMandatoryAlarm() && !enabled) {
            return false // 거부됨
        }

        val s = _ui.value
        _ui.value = s.copy(
            alarmSoundEnabled = enabled,
            isPreviewing = if (!enabled) false else s.isPreviewing
        )
        return true
    }

    fun selectSound(name: String, sound: AlarmSound) {
        _ui.value = _ui.value.copy(
            ringtoneName = name,
            sound = sound,
            isPreviewing = false
        )
    }

    fun toggleVibration(b: Boolean): Boolean {
        if (isMandatoryAlarm() && !b) {
            return false
        }
        _ui.value = _ui.value.copy(vibration = b)
        return true
    }

    fun updateVolume(v: Float): Boolean {
        val min = if (isMandatoryAlarm()) 0.2f else 0f
        val next = v.coerceIn(min, 1f)

        val rejected = isMandatoryAlarm() && v < min

        _ui.value = _ui.value.copy(volume = next)
        return !rejected
    }

    fun toggleFade(on: Boolean) {
        _ui.value = _ui.value.copy(fadeSeconds = if (on) 30 else 0)
    }

    fun toggleLoop(b: Boolean) {
        _ui.value = _ui.value.copy(loop = b)
    }

    fun toggleSnoozeEnabled(b: Boolean) {
        _ui.value = _ui.value.copy(snoozeEnabled = b)
    }

    fun selectSnooze(interval: Int, count: Int) {
        _ui.value = _ui.value.copy(snoozeInterval = interval, maxSnoozeCount = count)
    }

    fun togglePreview() {
        val s = _ui.value
        if (!s.alarmSoundEnabled) return
        _ui.value = s.copy(isPreviewing = !s.isPreviewing)
    }

    fun consumeError() {
        _ui.value = _ui.value.copy(errorMessage = null)
    }

    /* ---------- Save ---------- */
    fun save() = viewModelScope.launch {
        val s = _ui.value
        if (!s.canSave) return@launch
        _ui.value = s.copy(isBusy = true)
        try {
            val existing = repo.getAll()
            val isFirst = existing.isEmpty()

            val assignedId = if (isFirst) 1 else 0
            // 1 = mandatory alarm ID
            // 0 = auto-generate (Room auto ID)

            val alarm = Alarm(
                id = assignedId, // 신규
                hour = s.hour,
                minute = s.minute,
                days = s.repeatDays.indicesToDays(),
                skipHolidays = s.skipHolidays,
                enabled = true,

                sound = s.sound,
//                assetAudioPath = s.ringtonePath,
                alarmSoundEnabled = s.alarmSoundEnabled,
//                ringtoneName = s.ringtoneName,
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
            _events.emit(AddAlarmEvent.Saved)

        } catch (e: Exception) {
            val msg = "알람 저장 실패: ${e.message}"
            _ui.value = _ui.value.copy(errorMessage = msg)
            _events.emit(AddAlarmEvent.SaveFailed(msg))

        } finally {
            _ui.value = _ui.value.copy(isBusy = false)
        }
    }

    private fun isMandatoryAlarm(): Boolean = _ui.value.isFirstAlarm

    private fun recalcNextTrigger() {
        val s = _ui.value

        val nowLocal = timeProvider.nowLocal()
        val nowZdt = nowLocal.atZone(ZoneId.systemDefault())

        val probe = Alarm(
            id = 0,
            hour = s.hour,
            minute = s.minute,
            days = s.repeatDays.indicesToDays(),
            skipHolidays = s.skipHolidays,
            enabled = true,

            sound = s.sound,
//            assetAudioPath = s.ringtonePath,
            alarmSoundEnabled = s.alarmSoundEnabled,
//            ringtoneName = s.ringtoneName,
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

        val triggerMillis = nextTrigger.computeUtcMillis(probe, now = nowZdt)
        val line = NextTriggerFormatter.formatKor(triggerMillis, nowZdt)
        _ui.value = s.copy(nextTriggerText = line)
    }
}
