package lab.p4c.nextup.platform.telemetry.time

import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.system.dateKeyFromUtcEpochMillis
import lab.p4c.nextup.core.domain.telemetry.port.DateKeyProvider

@Singleton
class SystemDateKeyProvider @Inject constructor(
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val rolloverHour: Int = 3
) : DateKeyProvider {
    override fun fromUtcEpochMillis(timestampMsUtc: Long): String =
        dateKeyFromUtcEpochMillis(
            timestampMsUtc = timestampMsUtc,
            rolloverHour = rolloverHour,
            zone = zone
        )
}
