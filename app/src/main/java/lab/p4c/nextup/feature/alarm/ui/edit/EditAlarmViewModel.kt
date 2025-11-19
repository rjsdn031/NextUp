package lab.p4c.nextup.feature.alarm.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.alarm.ui.util.NextTriggerFormatter
import lab.p4c.nextup.core.common.time.daysToIndices
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.usecase.DeleteAlarmAndCancel
import lab.p4c.nextup.core.domain.alarm.usecase.UpsertAlarmAndReschedule
import lab.p4c.nextup.core.common.time.indicesToDays
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.core.domain.alarm.model.toTitle
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.ZoneId

data class EditAlarmUiState(
    val id: Int? = null,
    val loaded: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,

    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: List<Int> = emptyList(),      // 1=월..7=일
    val skipHolidays: Boolean = true,

    val alarmSoundEnabled: Boolean = true,
    val ringtoneName: String = "Test Alarm",
    val sound: AlarmSound = AlarmSound.Asset("test_sound"),

    val vibration: Boolean = true,
    val volume: Float = 1f,                       // 0..1
    val fadeSeconds: Int = 0,
    val loop: Boolean = true,

    val snoozeEnabled: Boolean = false,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = 3,

    val isPreviewing: Boolean = false,
    val nextTriggerText: String? = null
) {
    val canSave: Boolean =
        loaded && !isBusy && hour in 0..23 && minute in 0..59 && label.length <= 30
}

@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    private val repo: AlarmRepository,
    private val upsert: UpsertAlarmAndReschedule,   // 저장 + 재스케줄
    private val delete: DeleteAlarmAndCancel,        // 삭제 + 취소
    private val timeProvider: TimeProvider,
    private val nextTrigger: NextTriggerCalculator,
) : ViewModel() {

    private val _ui = MutableStateFlow(EditAlarmUiState())
    val ui = _ui.asStateFlow()

    private var baseline: String? = null
    private var hasLoaded = false

    fun load(alarmId: Int) = viewModelScope.launch {
        if (hasLoaded) return@launch
        hasLoaded = true

        _ui.value = _ui.value.copy(isBusy = true)
        try {
            val a: Alarm? = repo.getById(alarmId)
            if (a == null) {
                _ui.value = _ui.value.copy(errorMessage = "알람을 찾을 수 없습니다.", isBusy = false)
                return@launch
            }
            _ui.value = mapDomainToUi(a).copy(loaded = true, isBusy = false)
            snapshot()
            recalcNextTrigger()
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(errorMessage = e.message ?: "로딩 실패", isBusy = false)
        }
    }

    fun updateTime(h: Int, m: Int) {
        _ui.value = _ui.value.copy(hour = h, minute = m); recalcNextTrigger()
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

    fun toggleAlarmSound(enabled: Boolean) {
        val s = _ui.value
        _ui.value = s.copy(
            alarmSoundEnabled = enabled,
            isPreviewing = if (!enabled) false else s.isPreviewing
        )
    }

    fun selectSound(name: String, sound: AlarmSound) {
        _ui.value = _ui.value.copy(
            ringtoneName = name,
            sound = sound,
            isPreviewing = false
        )
    }

    fun toggleVibration(b: Boolean) {
        _ui.value = _ui.value.copy(vibration = b)
    }

    fun updateVolume(v: Float) {
        _ui.value = _ui.value.copy(volume = v.coerceIn(0f, 1f))
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

    /* ----- 저장/삭제 ----- */
    fun save(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val s = _ui.value
        val id = s.id ?: return@launch
        if (!s.canSave) return@launch

        _ui.value = s.copy(isBusy = true)
        try {
            val domain = mapUiToDomain(s.copy(id = id))
            upsert(domain)                         // ← 유즈케이스 호출(저장 + 재스케줄)
            baseline = snapshotValue(s.copy(id = id))
            onDone(true)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(errorMessage = "알람 저장 실패: ${e.message}")
            onDone(false)
        } finally {
            _ui.value = _ui.value.copy(isBusy = false)
        }
    }

    fun delete(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val id = _ui.value.id ?: return@launch
        _ui.value = _ui.value.copy(isBusy = true)
        try {
            delete(id)                             // ← 유즈케이스 호출
            onDone(true)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(errorMessage = "알람 삭제 실패: ${e.message}")
            onDone(false)
        } finally {
            _ui.value = _ui.value.copy(isBusy = false)
        }
    }

    /* ----- 변경사항 감지 ----- */
    fun hasUnsavedChanges(): Boolean = baseline != snapshot()
    private fun snapshot() = snapshotValue(_ui.value)
    private fun snapshotValue(s: EditAlarmUiState) = listOf(
        s.hour, s.minute, s.label.trim(),
        s.repeatDays.joinToString(","), s.skipHolidays,
        s.alarmSoundEnabled, s.ringtoneName, s.sound,
        s.vibration, s.volume, s.fadeSeconds, s.loop,
        s.snoozeEnabled, s.snoozeInterval, s.maxSnoozeCount
    ).joinToString("|")

    /* ----- 매핑/유틸 ----- */
    private fun recalcNextTrigger() {
        val s = _ui.value

        val zone = ZoneId.systemDefault()
        val nowZdt = timeProvider.nowLocal().atZone(zone)

        val domain = mapUiToDomain(
            s.copy(id = s.id ?: 0)
        )

        val triggerMillis = nextTrigger.computeUtcMillis(domain, nowZdt)
        val line = NextTriggerFormatter.formatKor(triggerMillis, nowZdt)

        _ui.value = s.copy(nextTriggerText = line)
    }

    /* mapper */
    private fun mapDomainToUi(a: Alarm) = EditAlarmUiState(
        id = a.id,
        hour = a.hour,
        minute = a.minute,
        label = a.name,
        repeatDays = a.days.daysToIndices(),
        skipHolidays = a.skipHolidays,

        sound = a.sound,
        alarmSoundEnabled = a.alarmSoundEnabled,
        ringtoneName = a.sound.toTitle(),
//        ringtonePath = a.assetAudioPath,

        vibration = a.vibration,
        volume = a.volume.toFloat(),
        fadeSeconds = a.fadeDuration,
        loop = a.loopAudio,

        snoozeEnabled = a.snoozeEnabled,
        snoozeInterval = a.snoozeInterval,
        maxSnoozeCount = a.maxSnoozeCount
    )

    /* mapper */
    private fun mapUiToDomain(s: EditAlarmUiState) = Alarm(
        id = s.id ?: 0,
        hour = s.hour,
        minute = s.minute,
        days = s.repeatDays.indicesToDays(),
        skipHolidays = s.skipHolidays,
        enabled = true,

        sound = s.sound,
//        assetAudioPath = s.ringtonePath,
        alarmSoundEnabled = s.alarmSoundEnabled,
//        ringtoneName = s.ringtoneName,
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
}
