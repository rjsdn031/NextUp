package lab.p4c.nextup.core.domain.telemetry.port

import lab.p4c.nextup.core.domain.telemetry.model.TelemetryRecord

interface TelemetrySink {
    fun write(record: TelemetryRecord)
}