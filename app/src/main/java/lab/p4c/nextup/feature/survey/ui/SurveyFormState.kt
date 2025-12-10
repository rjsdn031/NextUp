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

    data object ReasonTooShort : SurveyValidationError
    data object NextGoalTooShort : SurveyValidationError
}

// 타입 세이프 검증 결과
sealed interface Validation<out T> {
    data class Ok<T>(val value: T) : Validation<T>
    data class Err(val errors: List<SurveyValidationError>) : Validation<Nothing>
}

// 내부 유틸
private fun Int.isScoreValid(): Boolean = this in 0..4

fun SurveyFormState.validate(): List<SurveyValidationError> = buildList {

    // 1. 생산성 점수
    when (val s = productivityScore) {
        null -> add(SurveyValidationError.MissingProductivityScore)
        else -> if (!s.isScoreValid()) add(SurveyValidationError.InvalidProductivityScore)
    }

    // 2. 주관식 이유
    when {
        productivityReason.isBlank() ->
            add(SurveyValidationError.MissingReason)

        productivityReason.trim().length < 10 ->
            add(SurveyValidationError.ReasonTooShort)
    }

    // 3. 목표 달성 점수
    when (val g = goalAchievement) {
        null -> add(SurveyValidationError.MissingGoalAchievement)
        else -> if (!g.isScoreValid()) add(SurveyValidationError.InvalidGoalAchievement)
    }

    // 4. 내일 목표
    when {
        nextGoal.isBlank() ->
            add(SurveyValidationError.MissingNextGoal)

        nextGoal.trim().length < 10 ->
            add(SurveyValidationError.NextGoalTooShort)
    }
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
