package lab.p4c.nextup.feature.overlay.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import lab.p4c.nextup.core.domain.overlay.port.BlockTargetRepository

@Singleton
class BlockTargetRepositoryImpl @Inject constructor(
    private val store: BlockTargetStore
) : BlockTargetRepository {

    override fun observeTargets(): Flow<Set<String>> =
        store.observe()

    override suspend fun getTargets(): Set<String> =
        store.observe().first()

    override suspend fun setTargets(targets: Set<String>) {
        store.set(targets)
    }
}