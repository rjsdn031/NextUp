package lab.p4c.nextup.core.domain.blocking.usecase

import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import javax.inject.Inject

class GetBlockTargets @Inject constructor(
    private val repo: BlockTargetRepository
) {
    suspend operator fun invoke(): Set<String> {
        return repo.getTargets()
    }
}