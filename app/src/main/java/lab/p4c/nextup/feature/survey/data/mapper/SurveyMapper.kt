package lab.p4c.nextup.feature.survey.data.mapper

import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.feature.survey.data.local.entity.SurveyEntity

fun DailySurvey.toEntity() = SurveyEntity(
    dateKey = dateKey,
    missedYesterdayReason = missedYesterdayReason,
    sleepStartTime = sleepStartTime,
    sleepEndTime = sleepEndTime,
    sleepQualityScore = sleepQualityScore,
    productivityScore = productivityScore,
    productivityReason = productivityReason,
    goalAchievement = goalAchievement,
    nextGoal = nextGoal
)

fun SurveyEntity.toDomain() = DailySurvey(
    dateKey = dateKey,
    missedYesterdayReason = missedYesterdayReason,
    sleepStartTime = sleepStartTime,
    sleepEndTime = sleepEndTime,
    sleepQualityScore = sleepQualityScore,
    productivityScore = productivityScore,
    productivityReason = productivityReason,
    goalAchievement = goalAchievement,
    nextGoal = nextGoal
)

