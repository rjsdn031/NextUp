package lab.p4c.nextup.core.domain.telemetry.port

interface EventIdGenerator {
    fun newId(): String
}