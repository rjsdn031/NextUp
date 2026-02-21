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

@HiltViewModel
class SurveyScreenViewModel @Inject constructor(
    private val submitDailySurvey: SubmitDailySurvey,
    private val scheduleDailySurveyReminder: ScheduleDailySurveyReminder,
    private val updateTodayTargetFromSurvey: UpdateTodayTargetFromSurvey,
    private val timeProvider: TimeProvider,
    private val telemetryLogger: TelemetryLogger,
    private val surveyRepository: SurveyRepository
) : ViewModel() {

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

    private fun todayKey(): String = timeProvider.todaySurveyDateKey()
    private fun yesterdayKey(): String = timeProvider.yesterdaySurveyDateKey()

    fun onEnter() {
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

    private fun flow(): List<SurveyStep> =
        if (needsMissedReason) listOf(SurveyStep.MissedReason) + baseFlow else baseFlow

    fun isFirstStep(): Boolean = step == flow().first()
    fun isLastStep(): Boolean = step == flow().last()

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

    val canSubmit: Boolean
        get() = form.validate(needsMissedReason).isEmpty() && !isSubmitting

    fun primaryText(): String = if (isLastStep()) "제출" else "다음"

    fun primaryEnabled(): Boolean =
        if (isLastStep()) canSubmit else canGoNextCurrent()

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