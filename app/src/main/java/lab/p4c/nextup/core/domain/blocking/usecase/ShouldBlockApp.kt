package lab.p4c.nextup.core.domain.blocking.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import lab.p4c.nextup.feature.blocking.infra.BlockGate

class ShouldBlockApp @Inject constructor(
    private val repo: BlockTargetRepository,
    private val gate: BlockGate
) {
    suspend operator fun invoke(context: android.content.Context, pkg: String): Boolean {
        if (gate.isDisabled()) return false

        val targets = repo.getTargets()
        return pkg in targets
    }
}
