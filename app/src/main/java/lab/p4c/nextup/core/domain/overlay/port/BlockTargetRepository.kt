package lab.p4c.nextup.core.domain.overlay.port

import kotlinx.coroutines.flow.Flow

/**
 * 차단할 앱의 패키지명을 저장/조회하는 Repository
 */
interface BlockTargetRepository {
    suspend fun getTargets(): Set<String>
    suspend fun setTargets(targets: Set<String>)
    fun observeTargets(): Flow<Set<String>>
}