package lab.p4c.nextup.platform.telemetry.permission

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger

@Singleton
class PermissionChangeTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reader: PermissionSnapshotReader,
    private val telemetryLogger: TelemetryLogger
) {
    fun checkAndLog(source: String) {
        val now = reader.read()
        val prev = readPrev()

        val nowMap = now.asMap()
        val prevMap = prev?.asMap() ?: emptyMap()

        // 변화된 항목만 PermissionChanged 로깅
        for ((key, nowState) in nowMap) {
            val prevState = prevMap[key]
            if (prevState == null) {
                // 첫 진입은 저장만
                continue
            }
            if (prevState != nowState) {
                telemetryLogger.log(
                    eventName = "PermissionChanged",
                    payload = mapOf(
                        "TargetPermission" to key,
                        "State" to (if (nowState) "T" else "F"),
                        "source" to source
                    )
                )
            }
        }

        // 항상 최신 스냅샷 저장
        savePrev(now)
    }

    private fun prefs() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readPrev(): PermissionSnapshot? {
        val p = prefs()
        if (!p.contains("exact_alarm")) return null
        return PermissionSnapshot(
            exactAlarm = p.getBoolean("exact_alarm", false),
            overlay = p.getBoolean("overlay", false),
            accessibility = p.getBoolean("accessibility", false),
            usageAccess = p.getBoolean("usage_access", false),
            notification = p.getBoolean("notification", false),
            batteryIgnore = p.getBoolean("battery_ignore", false),
            microphone = p.getBoolean("microphone", false)
        )
    }

    private fun savePrev(s: PermissionSnapshot) {
        prefs().edit {
            putBoolean("exact_alarm", s.exactAlarm)
            putBoolean("overlay", s.overlay)
            putBoolean("accessibility", s.accessibility)
            putBoolean("usage_access", s.usageAccess)
            putBoolean("notification", s.notification)
            putBoolean("battery_ignore", s.batteryIgnore)
            putBoolean("microphone", s.microphone)
        }
    }

    private companion object {
        private const val PREFS = "telemetry_permission_state"
    }
}