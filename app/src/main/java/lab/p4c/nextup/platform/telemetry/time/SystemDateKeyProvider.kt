package lab.p4c.nextup.platform.telemetry.time

import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.system.dateKeyFromUtcEpochMillis
import lab.p4c.nextup.core.domain.telemetry.port.DateKeyProvider

@Singleton
class SystemDateKeyProvider @Inject constructor() : DateKeyProvider {

    override fun fromUtcEpochMillis(timestampMsUtc: Long): String {
        return dateKeyFromUtcEpochMillis(
            timestampMsUtc = timestampMsUtc,
            rolloverHour = ROLLOVER_HOUR,
            zone = ZONE
        )
    }

    private companion object {
        private const val ROLLOVER_HOUR = 3
        private val ZONE: ZoneId = ZoneId.systemDefault() // 또는 of("Asia/Seoul")
    }
}
