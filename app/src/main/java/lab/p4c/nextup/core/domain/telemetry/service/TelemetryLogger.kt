package lab.p4c.nextup.core.domain.telemetry.service

import javax.inject.Inject
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.telemetry.model.TelemetryRecord
import lab.p4c.nextup.core.domain.telemetry.port.DateKeyProvider
import lab.p4c.nextup.core.domain.telemetry.port.EventIdGenerator
import lab.p4c.nextup.core.domain.telemetry.port.TelemetrySink
import lab.p4c.nextup.core.domain.telemetry.port.UserIdProvider

class TelemetryLogger @Inject constructor(
    private val timeProvider: TimeProvider,
    private val userIdProvider: UserIdProvider,
    private val eventIdGenerator: EventIdGenerator,
    private val dateKeyProvider: DateKeyProvider,
    private val sink: TelemetrySink,
) {
    fun log(eventName: String, payload: Map<String, String> = emptyMap()) {
        val timestampMsUtc = timeProvider.now().toEpochMilli()
        val record = TelemetryRecord(
            userId = userIdProvider.getUserId(),
            eventId = eventIdGenerator.newId(),
            dateKey = dateKeyProvider.fromUtcEpochMillis(timestampMsUtc),
            timestampMsUtc = timestampMsUtc,
            eventName = eventName,
            payload = payload
        )
        sink.write(record)
    }
}
