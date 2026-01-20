package lab.p4c.nextup.feature.usage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity

@Dao
interface UsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sessions: List<UsageEntity>)

    @Query("""
        SELECT * FROM usage_session
        WHERE packageName = :packageName
          AND startMillis >= :startMs
          AND startMillis < :endMs
        ORDER BY startMillis ASC
    """)
    suspend fun getSessionsByWindow(
        packageName: String,
        startMs: Long,
        endMs: Long
    ): List<UsageEntity>

    @Query("""
        DELETE FROM usage_session
        WHERE dateKey < :dateKey
    """)
    suspend fun deleteBefore(dateKey: String)

    // 업로드 성공 후 "해당 dateKey" 데이터만 제거
    @Query("""
        DELETE FROM usage_session
        WHERE dateKey = :dateKey
    """)
    suspend fun deleteByDateKey(dateKey: String): Int

    @Query("""
    SELECT * FROM usage_session
    WHERE dateKey = :dateKey
    ORDER BY startMillis ASC
""")
    suspend fun getSessionsByDateKey(dateKey: String): List<UsageEntity>
}

