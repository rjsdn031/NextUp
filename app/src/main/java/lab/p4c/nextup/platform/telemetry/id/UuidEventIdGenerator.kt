package lab.p4c.nextup.platform.telemetry.id

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.EventIdGenerator

@Singleton
class UuidEventIdGenerator @Inject constructor() : EventIdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
