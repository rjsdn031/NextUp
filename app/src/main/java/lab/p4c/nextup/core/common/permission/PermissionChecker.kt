package lab.p4c.nextup.core.common.permission

interface PermissionChecker {
    fun hasExactAlarm(): Boolean
    fun hasOverlay(): Boolean
    fun hasUsageAccess(): Boolean
    fun hasNotification(): Boolean
    fun hasAccessibility(): Boolean
    fun hasMicrophone(): Boolean
}
