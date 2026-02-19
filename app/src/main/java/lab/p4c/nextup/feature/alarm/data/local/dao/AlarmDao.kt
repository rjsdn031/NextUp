package lab.p4c.nextup.feature.alarm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.feature.alarm.data.local.entity.AlarmEntity

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    suspend fun getAll(): List<AlarmEntity>

    @Insert
    suspend fun insert(entity: AlarmEntity): Long

    @Update
    suspend fun update(entity: AlarmEntity): Int

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun findByIdOrNull(id: Int): AlarmEntity?

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean): Int

    @Query(
        """
        SELECT * FROM alarms
        WHERE hour = :hour AND minute = :minute AND repeatMask = :repeatMask
          AND id != :excludeId
        """
    )
    suspend fun findByTimeAndDaysExceptId(
        hour: Int,
        minute: Int,
        repeatMask: Int,
        excludeId: Int
    ): List<AlarmEntity>

    @Query("DELETE FROM alarms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM alarms WHERE enabled = 1 ORDER BY hour, minute")
    suspend fun findEnabledAll(): List<AlarmEntity>

    /**
     * Upserts [incoming] and removes duplicates that share the same dedup key:
     * (hour, minute, repeatMask).
     *
     * Dedup rule:
     * - If [incoming.id] is 0, reuse the first duplicate's id when duplicates exist.
     * - Otherwise keep [incoming.id].
     * - After persistence, delete all other duplicates except the persisted id.
     *
     * @return persisted id and whether it was created.
     */
    @Transaction
    suspend fun upsertDedup(incoming: AlarmEntity): UpsertDedupResult {
        val excludeId = if (incoming.id == 0) -1 else incoming.id
        val duplicates = findByTimeAndDaysExceptId(
            hour = incoming.hour,
            minute = incoming.minute,
            repeatMask = incoming.repeatMask,
            excludeId = excludeId
        )

        val survivorId = when {
            incoming.id != 0 -> incoming.id
            duplicates.isNotEmpty() -> duplicates.first().id
            else -> 0
        }

        val toSave = if (survivorId == 0) incoming else incoming.copy(id = survivorId)

        val (persistedId, created) = if (toSave.id == 0) {
            insert(toSave).toInt() to true
        } else {
            val rows = update(toSave)
            if (rows == 0) insert(toSave).toInt() to true else toSave.id to false
        }

        if (duplicates.isNotEmpty()) {
            val toDelete = duplicates.map { it.id }.filterNot { it == persistedId }
            if (toDelete.isNotEmpty()) deleteByIds(toDelete)
        }

        return UpsertDedupResult(id = persistedId, created = created)
    }

    @Transaction
    suspend fun setEnabledAndGet(id: Int, enabled: Boolean): AlarmEntity? {
        val rows = setEnabled(id, enabled)
        if (rows == 0) return null
        return findByIdOrNull(id)
    }

    data class UpsertDedupResult(
        val id: Int,
        val created: Boolean
    )
}
