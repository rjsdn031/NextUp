package lab.p4c.nextup.feature.overlay.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.core.domain.overlay.model.TargetSource
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository

@Singleton
class OverlayTargetRepositoryImpl @Inject constructor(
    private val store: OverlayTargetStore
) : OverlayTargetRepository {

    private val defaultText = "실험에 성실하게 참여한다"

    override suspend fun setActiveGoal(text: String, source: TargetSource) {
        // TODO: Persist source if prioritization/debugging is needed later.
        store.setActiveGoal(text)
    }

    override suspend fun getActiveGoalOrDefault(): String {
        return store.observeActiveGoalText()
            .first()
            ?.takeIf { it.isNotBlank() }
            ?: defaultText
    }

    override fun observeActiveGoal(): Flow<String> {
        return store.observeActiveGoalText()
            .map { it?.takeIf(String::isNotBlank) ?: defaultText }
    }
}