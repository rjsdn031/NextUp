package lab.p4c.nextup.platform.telemetry.sink

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.model.TelemetryRecord
import lab.p4c.nextup.core.domain.telemetry.port.TelemetrySink

@Singleton
class JsonlTelemetrySink @Inject constructor(
    @ApplicationContext private val context: Context
) : TelemetrySink {

    private val lock = Any()

    override fun write(record: TelemetryRecord) {
        val line = toJsonLine(record)
        synchronized(lock) {
            file().appendText(line + "\n")
        }
    }

    private fun file(): File = File(context.filesDir, FILE_NAME)

    private fun toJsonLine(r: TelemetryRecord): String {
        val payloadJson = r.payload.entries.joinToString(
            prefix = "{",
            postfix = "}"
        ) { (k, v) -> "\"${escape(k)}\":\"${escape(v)}\"" }

        return buildString {
            append("{")
            append("\"UserId\":\"${escape(r.userId)}\",")
            append("\"EventId\":\"${escape(r.eventId)}\",")
            append("\"DateKey\":\"${escape(r.dateKey)}\",")
            append("\"timestampMsUtc\":${r.timestampMsUtc},")
            append("\"EventName\":\"${escape(r.eventName)}\",")
            append("\"payload\":$payloadJson")
            append("}")
        }
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

    private companion object {
        private const val FILE_NAME = "telemetry.jsonl"
    }
}
