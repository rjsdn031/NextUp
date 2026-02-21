package lab.p4c.nextup.feature.survey.ui

import lab.p4c.nextup.core.domain.survey.model.DailySurvey

/**
 * UI-layer state holder for the daily survey form.
 *
 * Notes:
 * - Time fields use the "HH:mm" 24-hour format.
 * - Score fields are stored as 0..4 in the UI and are converted to 1..5
 *   when mapped into the domain model ([DailySurvey]).
 * - Free-text fields are validated with a minimum length requirement to reduce
 *   low-effort responses in the experiment context.
 */
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

/**
 * Validation errors for [SurveyFormState].
 *
 * These errors are UI-facing and are intended to be mapped into human-readable
 * strings by the presentation layer.
 */
sealed interface SurveyValidationError {
    /** Missing "why missed yesterday survey" response when it is required. */
    data object MissingMissedYesterdayReason : SurveyValidationError

    /** "Why missed yesterday survey" response does not meet the minimum length requirement. */
    data object MissedYesterdayReasonTooShort : SurveyValidationError

    /** Missing sleep start time input. */
    data object MissingSleepStartTime : SurveyValidationError

    /** Missing sleep end time input. */
    data object MissingSleepEndTime : SurveyValidationError

    /** Sleep time inputs are not in the expected "HH:mm" format. */
    data object InvalidSleepTimeFormat : SurveyValidationError

    /** Missing sleep quality score selection. */
    data object MissingSleepQualityScore : SurveyValidationError

    /** Sleep quality score is outside the allowed UI range (0..4). */
    data object InvalidSleepQualityScore : SurveyValidationError

    /** Missing productivity score selection. */
    data object MissingProductivityScore : SurveyValidationError

    /** Missing free-text reason for the productivity score. */
    data object MissingReason : SurveyValidationError

    /** Missing goal achievement score selection. */
    data object MissingGoalAchievement : SurveyValidationError

    /** Missing next goal free-text input. */
    data object MissingNextGoal : SurveyValidationError

    /** Productivity score is outside the allowed UI range (0..4). */
    data object InvalidProductivityScore : SurveyValidationError

    /** Goal achievement score is outside the allowed UI range (0..4). */
    data object InvalidGoalAchievement : SurveyValidationError

    /** Productivity reason does not meet the minimum length requirement. */
    data object ReasonTooShort : SurveyValidationError
}

/**
 * Type-safe validation result.
 *
 * @param T Domain model type produced when validation succeeds.
 */
sealed interface Validation<out T> {
    /** Validation succeeded and contains the validated value. */
    data class Ok<T>(val value: T) : Validation<T>

    /** Validation failed and contains the list of errors. */
    data class Err(val errors: List<SurveyValidationError>) : Validation<Nothing>
}

/**
 * Returns whether a UI score value is within the allowed range (0..4).
 */
private fun Int.isScoreValid(): Boolean = this in 0..4

/**
 * Returns whether the receiver matches the expected "HH:mm" 24-hour time format.
 *
 * This is used for UI validation and should remain stable because
 * the stored data is persisted and later analyzed.
 */
internal fun String.isTimeHHmm(): Boolean {
    val r = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")
    return r.matches(this.trim())
}

/**
 * Validates this form state and returns a list of [SurveyValidationError].
 *
 * Validation rules:
 * - If [needsMissedReason] is true, [SurveyFormState.missedYesterdayReason] must be present
 *   and at least 10 characters.
 * - Sleep times must be present and match "HH:mm".
 * - Score selections must be present and within 0..4.
 * - Free-text fields ([SurveyFormState.productivityReason], [SurveyFormState.nextGoal])
 *   must be present and at least 10 characters.
 *
 * @param needsMissedReason Whether the "missed yesterday reason" question is part of the flow.
 */
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
    }
}

/**
 * Converts this UI state into the domain model [DailySurvey].
 *
 * Conversion rules:
 * - UI scores (0..4) are converted into domain scores (1..5) by adding 1.
 * - Strings are trimmed.
 * - If [needsMissedReason] is false, [DailySurvey.missedYesterdayReason] is set to null.
 *
 * @param dateKey Date key for the survey in "YYYY-MM-DD" format (project convention).
 * @param needsMissedReason Whether the "missed yesterday reason" question is part of the flow.
 */
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
