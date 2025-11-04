package lab.p4c.nextup.feature.survey.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import lab.p4c.nextup.feature.survey.data.local.entity.SurveyEntity

@Dao
interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: SurveyEntity)

    @Query("SELECT * FROM survey WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): SurveyEntity?
}