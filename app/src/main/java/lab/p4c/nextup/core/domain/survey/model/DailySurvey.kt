package lab.p4c.nextup.core.domain.survey.model

data class DailySurvey(
    val dateKey: String,

    val missedYesterdayReason: String?,

    val sleepStartTime: String, // "HH:mm"
    val sleepEndTime: String,   // "HH:mm"

    val sleepQualityScore: Int, // 1~5

    val productivityScore: Int,     // 1~5
    val productivityReason: String,  // 서술형
    val goalAchievement: Int,       // 1~5
    val nextGoal: String             // 서술형
)
