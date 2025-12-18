package lab.p4c.nextup.feature.blocking.infra

fun BlockGate.isBlockingActive(nowMillis: Long): Boolean = !isDisabled(nowMillis)
