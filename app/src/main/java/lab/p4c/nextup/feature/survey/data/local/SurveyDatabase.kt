package lab.p4c.nextup.feature.survey.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import lab.p4c.nextup.feature.survey.data.local.dao.SurveyDao
import lab.p4c.nextup.feature.survey.data.local.entity.SurveyEntity

@Database(
    entities = [SurveyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
}