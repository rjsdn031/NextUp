package lab.p4c.nextup.feature.alarm.data.local.dao

import androidx.room.*
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

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(id: Int, enabled: Boolean)

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun findByIdOrNull(id: Int): AlarmEntity?

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean): Int

    @Query("""
        SELECT * FROM alarms
        WHERE hour = :hour AND minute = :minute AND repeatMask = :repeatMask
          AND id != :excludeId
    """)
    suspend fun findByTimeAndDaysExceptId(
        hour: Int,
        minute: Int,
        repeatMask: Int,
        excludeId: Int
    ): List<AlarmEntity>

    @Query("DELETE FROM alarms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    fun findEnabledAll(): List<AlarmEntity>
}