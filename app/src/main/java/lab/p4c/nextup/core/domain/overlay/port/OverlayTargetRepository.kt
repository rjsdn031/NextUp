package lab.p4c.nextup.core.domain.overlay.port

import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.core.domain.overlay.model.TargetSource

interface OverlayTargetRepository {
    suspend fun setToday(text: String, source: TargetSource)
    suspend fun getActiveOrDefault(): String
    fun observeActive(): Flow<String>
}
