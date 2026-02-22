package lab.p4c.nextup.feature.survey.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.overlay.usecase.UpdateTodayTargetFromSurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import lab.p4c.nextup.core.domain.survey.usecase.SubmitDailySurvey
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.system.todaySurveyDateKey
import lab.p4c.nextup.core.domain.system.yesterdaySurveyDateKey
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.platform.telemetry.user.FirebaseUserIdProvider

/**
 * ViewModel for the daily survey screen.
 *
 * Responsibilities:
 * - Defines the step-based survey flow.
 * - Holds and mutates [SurveyFormState].
 * - Controls step navigation (Previous / Next / Submit).
 * - Performs validation before submission.
 * - Converts UI state into the domain model ([DailySurvey]).
 * - Triggers telemetry logging and post-submission side effects.
 *
 * This ViewModel intentionally:
 * - Does NOT auto-advance between steps.
 * - Separates navigation validation (canGoNextCurrent)
 *   from full-form validation (validate).
 * - Keeps flow definition centralized to guarantee consistent step ordering.
 */
@HiltViewModel
class SurveyScreenViewModel @Inject constructor(
    private val submitDailySurvey: SubmitDailySurvey,
    private val scheduleDailySurveyReminder: ScheduleDailySurveyReminder,
    private val updateTodayTargetFromSurvey: UpdateTodayTargetFromSurvey,
    private val timeProvider: TimeProvider,
    private val telemetryLogger: TelemetryLogger,
    private val surveyRepository: SurveyRepository,
    private val userIdProvider: FirebaseUserIdProvider,
) : ViewModel() {

    /**
     * Core survey flow excluding the optional "MissedReason" step.
     *
     * The actual flow is determined dynamically by [flow()],
     * depending on whether yesterday's survey was missing.
     */
    private val baseFlow = listOf(
        SurveyStep.SleepTime,
        SurveyStep.SleepQuality,
        SurveyStep.ProductivityScore,
        SurveyStep.ProductivityReason,
        SurveyStep.GoalAchievement,
        SurveyStep.NextGoal
    )

    var step by mutableStateOf(SurveyStep.MissedReason)
        private set

    var form by mutableStateOf(SurveyFormState())
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var needsMissedReason by mutableStateOf(false)
        private set

    private var startedLogged = false

    /**
     * True when the current session has no valid authenticated uid.
     * The UI layer should redirect the user to the experiment info screen.
     */
    var requiresAuth by mutableStateOf(false)
        private set

    private fun todayKey(): String = timeProvider.todaySurveyDateKey()
    private fun yesterdayKey(): String = timeProvider.yesterdaySurveyDateKey()

    /**
     * Called once when the screen enters composition.
     *
     * - Logs the "SurveyStarted" telemetry event.
     * - Determines whether the "MissedReason" step is required.
     * - Initializes the first step of the dynamic flow.
     */
    fun onEnter() {
        val uid = userIdProvider.getUserId()?.trim().orEmpty()
        if (uid.isEmpty()) {
            requiresAuth = true
            return
        }

        if (startedLogged) return
        startedLogged = true

        telemetryLogger.log(
            eventName = "SurveyStarted",
            payload = mapOf("DateKey" to todayKey())
        )

        viewModelScope.launch {
            needsMissedReason = surveyRepository.getByDate(yesterdayKey()) == null
            step = flow().first()
        }
    }

    fun onMissedYesterdayReason(t: String) {
        form = form.copy(missedYesterdayReason = t)
    }

    fun onSleepStartTime(t: String) {
        form = form.copy(sleepStartTime = t)
    }

    fun onSleepEndTime(t: String) {
        form = form.copy(sleepEndTime = t)
    }

    fun onSleepQualityScore(v: Int) {
        form = form.copy(sleepQualityScore = v)
    }

    fun onProductivityScore(v: Int) {
        form = form.copy(productivityScore = v)
    }

    fun onReason(t: String) {
        form = form.copy(productivityReason = t)
    }

    fun onGoalAchievement(v: Int) {
        form = form.copy(goalAchievement = v)
    }

    fun onNextGoal(t: String) {
        form = form.copy(nextGoal = t)
    }

    /**
     * Returns the ordered list of steps for the current session.
     *
     * If yesterday's survey is missing, the flow starts with
     * [SurveyStep.MissedReason], otherwise it starts with [SurveyStep.SleepTime].
     */
    private fun flow(): List<SurveyStep> =
        if (needsMissedReason) listOf(SurveyStep.MissedReason) + baseFlow else baseFlow

    fun isFirstStep(): Boolean = step == flow().first()
    fun isLastStep(): Boolean = step == flow().last()

    /**
     * Moves the current step by the given offset.
     *
     * @param offset +1 for next, -1 for previous.
     *
     * Navigation is bounded within the current dynamic flow.
     */
    private fun move(offset: Int) {
        if (isSubmitting) return

        val f = flow()
        val idx = f.indexOf(step)
        if (idx == -1) return

        val newIdx = idx + offset
        if (newIdx in f.indices) {
            step = f[newIdx]
        }
    }

    fun goPrev() = move(-1)

    fun goNext() {
        if (!canGoNextCurrent()) return
        move(1)
    }

    /**
     * Returns whether the user can proceed to the next step
     * based on the validation rule of the current step only.
     *
     * This is intentionally different from [canSubmit], which validates
     * the entire form.
     */
    fun canGoNextCurrent(): Boolean {
        if (isSubmitting) return false
        if (isLastStep()) return false

        return when (step) {
            SurveyStep.MissedReason ->
                form.missedYesterdayReason.trim().length >= 10

            SurveyStep.SleepTime ->
                form.sleepStartTime.isTimeHHmm() && form.sleepEndTime.isTimeHHmm()

            SurveyStep.SleepQuality ->
                form.sleepQualityScore != null

            SurveyStep.ProductivityScore ->
                form.productivityScore != null

            SurveyStep.ProductivityReason ->
                form.productivityReason.trim().length >= 10

            SurveyStep.GoalAchievement ->
                form.goalAchievement != null

            SurveyStep.NextGoal ->
                false
        }
    }

    /**
     * Returns true if the entire form passes validation
     * and no submission is currently in progress.
     */
    val canSubmit: Boolean
        get() = form.validate(needsMissedReason).isEmpty() && !isSubmitting

    fun primaryText(): String = if (isLastStep()) "제출" else "다음"

    fun primaryEnabled(): Boolean =
        if (isLastStep()) canSubmit else canGoNextCurrent()

    /**
     * Handles the primary CTA action.
     *
     * - If this is the last step → attempts submission.
     * - Otherwise → moves to the next step.
     */
    fun onPrimary(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) {
        if (isLastStep()) onSubmit(onSuccess, onError) else goNext()
    }

    fun onSubmit(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) {
        if (!isLastStep() || !canSubmit) return
        submit(onSuccess, onError)
    }

    /**
     * Executes the submission pipeline:
     *
     * 1. Validates and converts UI state into [DailySurvey].
     * 2. Persists the survey via [SubmitDailySurvey].
     * 3. Logs "SurveyCompleted" telemetry.
     * 4. Updates today's target if provided.
     * 5. Schedules the next daily survey reminder.
     * 6. Resets UI state to the first step.
     *
     * Submission is guarded by [isSubmitting] to prevent duplicates.
     */
    private fun submit(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) = viewModelScope.launch {
        if (isSubmitting) return@launch
        isSubmitting = true

        try {
            when (val r = form.toDomain(todayKey(), needsMissedReason)) {
                is Validation.Ok -> {
                    submitDailySurvey(r.value)

                    telemetryLogger.log(
                        eventName = "SurveyCompleted",
                        payload = mapOf("DateKey" to todayKey())
                    )

                    if (form.nextGoal.isNotBlank()) {
                        updateTodayTargetFromSurvey(form.nextGoal)
                    }

                    scheduleDailySurveyReminder()
                    onSuccess()

                    step = flow().first()
                    form = SurveyFormState()
                }

                is Validation.Err -> onError(r.errors)
            }
        } finally {
            isSubmitting = false
        }
    }
}