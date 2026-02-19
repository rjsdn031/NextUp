package lab.p4c.nextup.core.domain.alarm.port

import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.core.domain.alarm.model.Alarm



/**
 * Repository port for persisting and retrieving [Alarm] configurations.
 *
 * This interface is a domain-level contract. Implementations live in feature/data layers
 * (e.g., Room, DataStore) and must map persistence models to [Alarm].
 *
 * Observability:
 * - [observe] should emit the latest list whenever any alarm changes.
 * - Emissions are expected to be ordered consistently (implementation-defined) to avoid UI churn.
 *
 * Concurrency:
 * - All operations must be safe under concurrent calls.
 */
interface AlarmRepository {

    data class UpsertResult(
        val alarm: Alarm,
        val created: Boolean
    )
    /**
     * Observes all alarms as a stream.
     *
     * The returned [Flow] should be hot or cold depending on implementation, but it must:
     * - Emit an initial value promptly.
     * - Emit again whenever alarms are added/updated/deleted.
     */
    fun observe(): Flow<List<Alarm>>

    /**
     * Returns the current snapshot of all alarms.
     */
    suspend fun getAll(): List<Alarm>

    /**
     * Returns all alarms that are currently enabled.
     */
    suspend fun getEnabledAll(): List<Alarm>

    /**
     * Inserts or updates the given [alarm].
     *
     * The persistence identifier is [Alarm.id]. Implementations should treat equal ids as upserts.
     */
    suspend fun upsert(alarm: Alarm): UpsertResult

    /**
     * Deletes an alarm by its identifier.
     *
     * If the id does not exist, implementations may treat this as a no-op.
     */
    suspend fun delete(id: Int)

    /**
     * Returns an alarm by its identifier, or null if not found.
     */
    suspend fun getById(id: Int): Alarm?

    /**
     * Sets [enabled] state for the alarm with the given [id].
     *
     * @return true if an alarm with [id] existed and was updated, false otherwise.
     *
     * TODO(refactor):
     * - Consider exposing a more general update API (e.g., update(id) { ... }) if partial updates grow.
     */
    suspend fun setEnabled(id: Int, enabled: Boolean): Boolean

    /**
     * Sets enabled state and returns the updated alarm, or null if id not found.
     */
    suspend fun setEnabledAndGet(id: Int, enabled: Boolean): Alarm?
}
