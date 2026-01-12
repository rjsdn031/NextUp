package lab.p4c.nextup.platform.telemetry.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.platform.permission.AccessibilityPermission
import lab.p4c.nextup.platform.permission.BatteryOptimizationPermission
import lab.p4c.nextup.platform.permission.ExactAlarmPermission
import lab.p4c.nextup.platform.permission.MicrophonePermission
import lab.p4c.nextup.platform.permission.NotificationPermission
import lab.p4c.nextup.platform.permission.OverlayPermission
import lab.p4c.nextup.platform.permission.UsageAccessPermission

@Singleton
class PermissionSnapshotReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun read(): PermissionSnapshot = PermissionSnapshot(
        exactAlarm = ExactAlarmPermission.canSchedule(context),
        overlay = OverlayPermission.canDraw(context),
        accessibility = AccessibilityPermission.isEnabled(context),
        usageAccess = UsageAccessPermission.isGranted(context),
        notification = NotificationPermission.isGranted(context),
        batteryIgnore = BatteryOptimizationPermission.isIgnoring(context),
        microphone = MicrophonePermission.isGranted(context)
    )
}