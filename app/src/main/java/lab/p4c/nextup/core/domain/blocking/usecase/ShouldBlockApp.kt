package lab.p4c.nextup.core.domain.blocking.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import lab.p4c.nextup.feature.blocking.infra.BlockGate

class ShouldBlockApp @Inject constructor(
    private val repo: BlockTargetRepository,
    private val gate: BlockGate
) {
    suspend operator fun invoke(pkg: String, nowMillis: Long): Boolean {
        // BlockGate 내부에서 timestamp 기반 전체 disable 판단
        if (gate.isDisabled(nowMillis)) return false

        val targets = repo.getTargets()
        return pkg in targets
    }
}