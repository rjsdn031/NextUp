package lab.p4c.nextup.feature.blocking.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockTargetRepositoryImpl @Inject constructor(
    private val store: BlockTargetStore
) : BlockTargetRepository {

    override fun observeTargets(): Flow<Set<String>> =
        store.observe().map { saved ->
            when {
                saved == null -> emptySet()
                saved.isEmpty() -> emptySet()
                else -> saved
            }
        }

    override suspend fun getTargets(): Set<String> =
        observeTargets().first()

    override suspend fun setTargets(targets: Set<String>) {
        store.set(targets)
    }
}