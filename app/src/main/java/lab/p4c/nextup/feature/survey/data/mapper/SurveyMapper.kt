package lab.p4c.nextup.feature.survey.data.mapper

import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.feature.survey.data.local.entity.SurveyEntity

fun DailySurvey.toEntity() = SurveyEntity(
    dateKey, productivityScore, productivityReason, goalAchievement, nextGoal
)

fun SurveyEntity.toDomain() = DailySurvey(
    dateKey, productivityScore, productivityReason, goalAchievement, nextGoal
)
