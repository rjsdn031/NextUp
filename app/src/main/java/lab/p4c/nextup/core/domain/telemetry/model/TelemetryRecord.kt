package lab.p4c.nextup.core.domain.telemetry.model

data class TelemetryRecord(
    val userId: String,
    val eventId: String,
    val dateKey: String,
    val timestampMsUtc: Long,
    val eventName: String,
    val payload: Map<String, String> = emptyMap()
)