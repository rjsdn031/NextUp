package lab.p4c.nextup.feature.survey.ui

import lab.p4c.nextup.core.domain.survey.model.DailySurvey

data class SurveyFormState(
    val productivityScore: Int? = null, // 0..4
    val productivityReason: String = "",
    val goalAchievement: Int? = null,   // 0..4
    val nextGoal: String = ""
)

sealed interface SurveyValidationError {
    data object MissingProductivityScore : SurveyValidationError
    data object MissingReason : SurveyValidationError
    data object MissingGoalAchievement : SurveyValidationError
    data object MissingNextGoal : SurveyValidationError
    data object InvalidProductivityScore : SurveyValidationError // 범위 벗어남
    data object InvalidGoalAchievement : SurveyValidationError   // 범위 벗어남
}

// 타입 세이프 검증 결과
sealed interface Validation<out T> {
    data class Ok<T>(val value: T) : Validation<T>
    data class Err(val errors: List<SurveyValidationError>) : Validation<Nothing>
}

// 내부 유틸
private fun Int.isScoreValid(): Boolean = this in 0..4

fun SurveyFormState.validate(): List<SurveyValidationError> = buildList {
    when (val s = productivityScore) {
        null -> add(SurveyValidationError.MissingProductivityScore)
        else -> if (!s.isScoreValid()) add(SurveyValidationError.InvalidProductivityScore)
    }
    if (productivityReason.isBlank()) add(SurveyValidationError.MissingReason)

    when (val g = goalAchievement) {
        null -> add(SurveyValidationError.MissingGoalAchievement)
        else -> if (!g.isScoreValid()) add(SurveyValidationError.InvalidGoalAchievement)
    }
    if (nextGoal.isBlank()) add(SurveyValidationError.MissingNextGoal)
}

fun SurveyFormState.toDomain(dateKey: String): Validation<DailySurvey> {
    val errors = validate()
    if (errors.isNotEmpty()) return Validation.Err(errors)

    return Validation.Ok(
        DailySurvey(
            dateKey = dateKey,
            productivityScore = productivityScore!!,
            productivityReason = productivityReason.trim(),
            goalAchievement = goalAchievement!!,
            nextGoal = nextGoal.trim()
        )
    )
}
