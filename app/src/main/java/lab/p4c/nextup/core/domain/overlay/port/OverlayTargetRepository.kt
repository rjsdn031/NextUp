package lab.p4c.nextup.core.domain.overlay.port

import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.core.domain.overlay.model.TargetSource

/**
 * Provides the active goal used by the blocking overlay.
 *
 * The active goal is a single effective value that remains valid until overwritten.
 */
interface OverlayTargetRepository {

    /**
     * Sets the active goal text.
     *
     * @param text Active goal text.
     * @param source Source of the goal (e.g., survey).
     */
    suspend fun setActiveGoal(text: String, source: TargetSource)

    /**
     * Returns the active goal text if available, otherwise returns a default.
     */
    suspend fun getActiveGoalOrDefault(): String

    /**
     * Observes the active goal text. Always emits a non-blank string by applying default fallback.
     */
    fun observeActiveGoal(): Flow<String>
}