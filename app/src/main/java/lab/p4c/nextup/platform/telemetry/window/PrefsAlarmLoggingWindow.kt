package lab.p4c.nextup.platform.telemetry.window

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.AlarmLoggingWindow

@Singleton
class PrefsAlarmLoggingWindow @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmLoggingWindow {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    override fun markAlarmTriggered(timestampMsUtc: Long) {
        prefs.edit { putLong(KEY_LAST_TRIGGERED_UTC, timestampMsUtc) }
    }

    override fun isWithinWindow(nowMsUtc: Long, windowMs: Long): Boolean {
        val last = prefs.getLong(KEY_LAST_TRIGGERED_UTC, 0L)
        if (last <= 0L) return false
        return nowMsUtc in last..(last + windowMs)
    }

    private companion object {
        private const val PREFS = "telemetry_window"
        private const val KEY_LAST_TRIGGERED_UTC = "last_alarm_triggered_utc"
    }
}
