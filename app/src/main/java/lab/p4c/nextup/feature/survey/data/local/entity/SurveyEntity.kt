package lab.p4c.nextup.feature.survey.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey")
data class SurveyEntity(
    @PrimaryKey val dateKey: String,

    val missedYesterdayReason: String?,

    val sleepStartTime: String, // "HH:mm"
    val sleepEndTime: String,   // "HH:mm"

    val sleepQualityScore: Int,

    val productivityScore: Int,
    val productivityReason: String,
    val goalAchievement: Int,
    val nextGoal: String
)
