package lab.p4c.nextup.core.domain.experiment.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository

/**
 * Returns whether the experiment session is considered "active".
 *
 * In NextUp, the experiment becomes active once the user selects at least one
 * blocking target app. This flag is used to gate survey entry points and
 * reminder scheduling.
 */
class IsExperimentActive @Inject constructor(
    private val blockTargets: BlockTargetRepository
) {
    operator fun invoke(): Flow<Boolean> =
        blockTargets.observeTargets().map { it.isNotEmpty() }
}