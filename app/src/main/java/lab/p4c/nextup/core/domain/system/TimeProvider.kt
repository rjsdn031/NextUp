package lab.p4c.nextup.core.domain.system

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface TimeProvider {
    fun now(): Instant
    fun nowLocal(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime
}