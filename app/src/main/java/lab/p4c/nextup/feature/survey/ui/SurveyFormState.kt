package lab.p4c.nextup.feature.survey.ui

import lab.p4c.nextup.core.domain.survey.model.DailySurvey

data class SurveyFormState(
    val missedYesterdayReason: String = "",
    val sleepStartTime: String = "", // "HH:mm"
    val sleepEndTime: String = "",   // "HH:mm"
    val sleepQualityScore: Int? = null, // 0..4
    val productivityScore: Int? = null, // 0..4
    val productivityReason: String = "",
    val goalAchievement: Int? = null,   // 0..4
    val nextGoal: String = ""
)

sealed interface SurveyValidationError {
    data object MissingMissedYesterdayReason : SurveyValidationError
    data object MissedYesterdayReasonTooShort : SurveyValidationError

    data object MissingSleepStartTime : SurveyValidationError
    data object MissingSleepEndTime : SurveyValidationError
    data object InvalidSleepTimeFormat : SurveyValidationError

    data object MissingSleepQualityScore : SurveyValidationError
    data object InvalidSleepQualityScore : SurveyValidationError
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

private fun String.isTimeHHmm(): Boolean {
    val r = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")
    return r.matches(this.trim())
}

fun SurveyFormState.validate(needsMissedReason: Boolean): List<SurveyValidationError> = buildList {

    // (어제 설문을 안했다면) 왜 설문을 안했는지
    if (needsMissedReason) {
        when {
            missedYesterdayReason.isBlank() -> add(SurveyValidationError.MissingMissedYesterdayReason)
            missedYesterdayReason.trim().length < 10 -> add(SurveyValidationError.MissedYesterdayReasonTooShort)
        }
    }

    // 수면 자가보고
    when {
        sleepStartTime.isBlank() -> add(SurveyValidationError.MissingSleepStartTime)
        sleepEndTime.isBlank() -> add(SurveyValidationError.MissingSleepEndTime)
        !sleepStartTime.isTimeHHmm() || !sleepEndTime.isTimeHHmm() -> add(SurveyValidationError.InvalidSleepTimeFormat)
    }

    // 수면 질
    when (val s = sleepQualityScore) {
        null -> add(SurveyValidationError.MissingSleepQualityScore)
        else -> if (!s.isScoreValid()) add(SurveyValidationError.InvalidSleepQualityScore)
    }

    // 생산성 점수
    when (val s = productivityScore) {
        null -> add(SurveyValidationError.MissingProductivityScore)
        else -> if (!s.isScoreValid()) add(SurveyValidationError.InvalidProductivityScore)
    }

    // 주관식 이유
    when {
        productivityReason.isBlank() ->
            add(SurveyValidationError.MissingReason)

        productivityReason.trim().length < 10 ->
            add(SurveyValidationError.ReasonTooShort)
    }

    // 목표 달성 점수
    when (val g = goalAchievement) {
        null -> add(SurveyValidationError.MissingGoalAchievement)
        else -> if (!g.isScoreValid()) add(SurveyValidationError.InvalidGoalAchievement)
    }

    // 내일 목표
    when {
        nextGoal.isBlank() ->
            add(SurveyValidationError.MissingNextGoal)

        nextGoal.trim().length < 10 ->
            add(SurveyValidationError.NextGoalTooShort)
    }
}

fun SurveyFormState.toDomain(dateKey: String, needsMissedReason: Boolean): Validation<DailySurvey> {
    val errors = validate(needsMissedReason)
    if (errors.isNotEmpty()) return Validation.Err(errors)

    return Validation.Ok(
        DailySurvey(
            dateKey = dateKey,
            missedYesterdayReason = if (needsMissedReason) missedYesterdayReason.trim() else null,
            sleepStartTime = sleepStartTime.trim(),
            sleepEndTime = sleepEndTime.trim(),
            sleepQualityScore = sleepQualityScore!! + 1,   // 1..5
            productivityScore = productivityScore!! + 1,   // 1..5
            productivityReason = productivityReason.trim(),
            goalAchievement = goalAchievement!! + 1,        // 1..5
            nextGoal = nextGoal.trim()
        )
    )
}
