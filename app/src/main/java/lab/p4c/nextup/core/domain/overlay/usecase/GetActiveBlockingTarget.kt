package lab.p4c.nextup.core.domain.overlay.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository

class GetActiveBlockingTarget @Inject constructor(
    private val repo: OverlayTargetRepository
) {
    suspend operator fun invoke(): String = repo.getActiveOrDefault()
}
