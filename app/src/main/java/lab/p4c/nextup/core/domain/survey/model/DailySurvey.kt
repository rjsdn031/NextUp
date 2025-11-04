package lab.p4c.nextup.core.domain.survey.model

data class DailySurvey(
    val dateKey: String,
    val productivityScore: Int,     // 0~4
    val productivityReason: String,  // 서술형
    val goalAchievement: Int,       // 0~4
    val nextGoal: String             // 서술형
)
