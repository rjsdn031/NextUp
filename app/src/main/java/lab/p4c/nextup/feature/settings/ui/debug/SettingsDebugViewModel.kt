package lab.p4c.nextup.feature.settings.ui.debug

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.survey.usecase.DebugScheduleSurveyReminder
import lab.p4c.nextup.feature.uploader.infra.scheduler.UploadAlarmScheduler
import java.time.ZoneId

@HiltViewModel
class SettingsDebugViewModel @Inject constructor(
    private val debugScheduleSurveyReminder: DebugScheduleSurveyReminder,
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsDebugUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun scheduleSurveyReminderInOneMinute() = viewModelScope.launch {
        val zone = ZoneId.systemDefault()
        val reminderTimes = debugScheduleSurveyReminder.scheduleInMinute(
            offsetInMinutes = 1,
            zone = zone,
        )

        val message = reminderTimes
            .joinToString(", ") { it.toString() }

        _events.tryEmit(
            SettingsDebugUiEvent.Toast("테스트 알림을 예약했습니다: $message")
        )
    }

    fun triggerUploaderInSeconds(context: Context, seconds: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            _events.tryEmit(SettingsDebugUiEvent.Toast("Android 12+에서만 지원"))
            return
        }

        UploadAlarmScheduler.scheduleInSecondsForDebug(context, seconds)
        _events.tryEmit(SettingsDebugUiEvent.Toast("업로더 테스트 트리거(${seconds}초) 예약"))
    }
}