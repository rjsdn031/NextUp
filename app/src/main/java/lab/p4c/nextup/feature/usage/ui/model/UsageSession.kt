package lab.p4c.nextup.feature.usage.ui.model

data class UsageSession(
    val startMillis: Long,
    val endMillis: Long,
    val packageName: String
) {
    val durationMillis: Long get() = (endMillis - startMillis).coerceAtLeast(0L)
}