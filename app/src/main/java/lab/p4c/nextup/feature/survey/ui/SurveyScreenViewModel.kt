package lab.p4c.nextup.feature.survey.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.survey.usecase.SubmitDailySurvey
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

    var step by mutableIntStateOf(1)
        private set

    var form by mutableStateOf(SurveyFormState())
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    private var startedLogged by mutableStateOf(false)
    private var completedLogged by mutableStateOf(false)

    private object Steps {
        const val MissedReason = 1
        const val SleepTime = 2
        const val SleepQuality = 3
        const val ProductivityScore = 4
        const val ProductivityReason = 5
        const val GoalAchievement = 6
        const val NextGoal = 7
    }

    var needsMissedReason by mutableStateOf(false)
        private set

    private fun todayKey(): String = timeProvider.todaySurveyDateKey()
    private fun yesterdayKey(): String = timeProvider.yesterdaySurveyDateKey()
    /**
     * 설문 화면 진입 시 1회 호출 (SurveyStarted)
     */
    fun onEnter() {
        if (startedLogged) return
        startedLogged = true

        telemetryLogger.log(
            eventName = "SurveyStarted",
            payload = mapOf("DateKey" to todayKey())
        )

        viewModelScope.launch {
            needsMissedReason = surveyRepository.getByDate(yesterdayKey()) == null

            step = if (needsMissedReason) Steps.MissedReason else Steps.SleepTime
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
        if (step == Steps.SleepQuality) step = Steps.ProductivityScore
    }

    fun onProductivityScore(v: Int) {
        form = form.copy(productivityScore = v)
        if (step == Steps.ProductivityScore) step = Steps.ProductivityReason
    }

    fun onReason(t: String) {
        form = form.copy(productivityReason = t)
        // 주관식은 여기서 step을 올리지 않는다.
        // QuestionCardText 내부 '다음' 버튼에서 onNext()를 호출.
    }

    fun onGoalAchievement(v: Int) {
        form = form.copy(goalAchievement = v)
        if (step == Steps.GoalAchievement) step = Steps.NextGoal
    }

    fun onNextGoal(t: String) {
        form = form.copy(nextGoal = t)
        // 마지막 주관식도 입력만 반영,
        // 제출은 QuestionCardText 내부 '제출' 버튼에서 onSubmit() 호출.
    }

    val canSubmit: Boolean
        get() = form.validate(needsMissedReason).isEmpty() && !isSubmitting

    /**
     * 주관식 카드(QuestionCardText)에서 '다음' 버튼이 눌렸을 때 호출.
     */
    fun onNext() {
        if (isSubmitting) return
        step = nextStep(step)
    }

    private fun nextStep(current: Int): Int {
        return when (current) {
            Steps.MissedReason -> Steps.SleepTime
            Steps.SleepTime -> Steps.SleepQuality
            Steps.ProductivityReason -> Steps.GoalAchievement
            else -> current
        }
    }

    /**
     * 마지막 문항에서 '제출' 버튼이 눌렸을 때 호출.
     * (step == 4 && canProceed)일 때만 submit 실행.
     */

    fun onSubmit(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) {
        if (step != Steps.NextGoal || !canSubmit) return
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

                    if (!completedLogged) {
                        completedLogged = true
                        telemetryLogger.log(
                            eventName = "SurveyCompleted",
                            payload = mapOf("DateKey" to todayKey())
                        )
                    }

                    if (form.nextGoal.isNotBlank()) {
                        updateTodayTargetFromSurvey(form.nextGoal)
                    }

                    scheduleDailySurveyReminder()
                    onSuccess()

                    step = if (needsMissedReason) Steps.MissedReason else Steps.SleepTime
                    form = SurveyFormState()
                }

                is Validation.Err -> onError(r.errors)
            }
        } finally {
            isSubmitting = false
        }
    }
}
