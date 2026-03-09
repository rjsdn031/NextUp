package lab.p4c.nextup.feature.alarm.ui.edit

import android.content.Context
import android.media.RingtoneManager
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import lab.p4c.nextup.feature.alarm.ui.picker.UriNameResolver
import lab.p4c.nextup.feature.alarm.ui.picker.defaultAppSounds
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

    val snoozeEnabled: Boolean = true,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = Int.MAX_VALUE,

    val isPreviewing: Boolean = false,
    val nextTriggerText: String? = null
) {
    val canSave: Boolean =
        loaded && !isBusy && hour in 0..23 && minute in 0..59 && label.length <= 30
}

@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: AlarmRepository,
    private val upsert: UpsertAlarmAndReschedule,
    private val delete: DeleteAlarmAndCancel,
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

    private fun isMandatoryAlarm(): Boolean = _ui.value.id == 1

    fun updateTime(h: Int, m: Int) {
        _ui.value = _ui.value.copy(hour = h, minute = m); recalcNextTrigger()
    }

    fun updateLabel(s: String) {
        _ui.value = _ui.value.copy(label = s)
    }

    fun updateDays(days: List<Int>): Boolean {
        if (isMandatoryAlarm()) return false
        _ui.value = _ui.value.copy(repeatDays = days)
        recalcNextTrigger()
        return true
    }


    fun toggleSkipHolidays(b: Boolean): Boolean {
        if (isMandatoryAlarm()) return false
        _ui.value = _ui.value.copy(skipHolidays = b)
        recalcNextTrigger()
        return true
    }

    fun toggleAlarmSound(enabled: Boolean): Boolean {
        if (isMandatoryAlarm() && !enabled) return false

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
        if (isMandatoryAlarm() && !b) return false
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

    fun toggleSnoozeEnabled(b: Boolean): Boolean {
        if (isMandatoryAlarm()) return false
        _ui.value = _ui.value.copy(snoozeEnabled = b)
        return true
    }

    fun selectSnooze(interval: Int, count: Int): Boolean {
        if (isMandatoryAlarm()) return false
        _ui.value = _ui.value.copy(snoozeInterval = interval, maxSnoozeCount = count)
        return true
    }

    /* ----- 저장/삭제 ----- */
    fun save(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val s = _ui.value
        val id = s.id ?: return@launch
        if (!s.canSave) return@launch

        _ui.value = s.copy(isBusy = true)
        try {
            val fixed = if (id == 1) {
                s.copy(
                    alarmSoundEnabled = true,
                    vibration = true,
                    volume = s.volume.coerceIn(0.2f, 1f),
                    snoozeEnabled = true,
                    snoozeInterval = 5,
                    maxSnoozeCount = Int.MAX_VALUE,
                )
            } else s

            val domain = mapUiToDomain(fixed.copy(id = id))
            upsert(domain)
            baseline = snapshotValue(fixed.copy(id = id))
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
            delete(id)
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
//        ringtoneName = a.sound.toTitle(), // TODO: toTitle 구조 바꾸기
        ringtoneName = resolveSoundTitle(a.sound),
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

    private fun resolveSoundTitle(sound: AlarmSound): String {
        return when (sound) {
            is AlarmSound.Asset -> {
                defaultAppSounds.firstOrNull { it.sound == sound }?.title ?: sound.resName
            }
            is AlarmSound.System -> {
                ringtoneTitle(sound.uri) ?: "시스템 알람음"
            }
            is AlarmSound.Custom -> {
                UriNameResolver.displayName(appContext, sound.uri.toUri())
                    ?: ringtoneTitle(sound.uri)
                    ?: "사용자 파일"
            }
        }
    }

    private fun ringtoneTitle(uri: String): String? {
        return runCatching {
            RingtoneManager.getRingtone(appContext, uri.toUri())?.getTitle(appContext)
        }.getOrNull()
    }
}
