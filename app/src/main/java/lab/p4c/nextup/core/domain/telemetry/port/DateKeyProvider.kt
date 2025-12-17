package lab.p4c.nextup.core.domain.telemetry.port

interface DateKeyProvider {
    fun fromUtcEpochMillis(timestampMsUtc: Long): String
}
