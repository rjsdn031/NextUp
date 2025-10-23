package lab.p4c.nextup.app.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import lab.p4c.nextup.core.domain.system.TimeProvider

class SystemTimeProvider @Inject constructor(
    private val clock: Clock
) : TimeProvider {
    override fun now(): Instant = clock.instant()
    override fun nowLocal(zone: ZoneId): LocalDateTime = LocalDateTime.now(clock.withZone(zone))
}