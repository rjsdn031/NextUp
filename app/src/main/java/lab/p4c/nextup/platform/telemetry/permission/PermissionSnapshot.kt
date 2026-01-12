package lab.p4c.nextup.platform.telemetry.permission

data class PermissionSnapshot(
    val exactAlarm: Boolean,
    val overlay: Boolean,
    val accessibility: Boolean,
    val usageAccess: Boolean,
    val notification: Boolean,
    val batteryIgnore: Boolean,
    val microphone: Boolean
) {
    fun asMap(): Map<String, Boolean> = mapOf(
        "exact_alarm" to exactAlarm,
        "overlay" to overlay,
        "accessibility" to accessibility,
        "usage_access" to usageAccess,
        "notification" to notification,
        "battery_ignore" to batteryIgnore,
        "microphone" to microphone
    )
}
