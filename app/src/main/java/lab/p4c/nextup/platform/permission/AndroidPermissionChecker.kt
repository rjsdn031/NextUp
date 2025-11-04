// platform/permission/AndroidPermissionChecker.kt
package lab.p4c.nextup.platform.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.common.permission.PermissionChecker

@Singleton
class AndroidPermissionChecker @Inject constructor(
    @param: ApplicationContext private val context: Context
) : PermissionChecker {

    override fun hasExactAlarm(): Boolean =
        ExactAlarmPermission.canSchedule(context)

    override fun hasOverlay(): Boolean =
        OverlayPermission.canDraw(context)

    override fun hasUsageAccess(): Boolean =
        UsageAccessPermission.isGranted(context)

    override fun hasNotification(): Boolean =
        NotificationPermission.isGranted(context)

    override fun hasAccessibility(): Boolean =
        AccessibilityPermission.isEnabled(context)

    override fun hasMicrophone(): Boolean =
        MicrophonePermission.isGranted(context)
}
