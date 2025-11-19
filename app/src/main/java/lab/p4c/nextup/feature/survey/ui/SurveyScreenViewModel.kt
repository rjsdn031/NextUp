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
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.survey.usecase.SubmitDailySurvey
import java.time.ZoneId

@HiltViewModel
class SurveyScreenViewModel @Inject constructor(
    private val submitDailySurvey: SubmitDailySurvey,
    private val scheduleDailySurveyReminder: ScheduleDailySurveyReminder,
    private val updateTodayTargetFromSurvey: UpdateTodayTargetFromSurvey,
    private val timeProvider: TimeProvider
) : ViewModel() {

    var step by mutableIntStateOf(1)
        private set

    var form by mutableStateOf(SurveyFormState())
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    fun onProductivityScore(v: Int) {
        form = form.copy(productivityScore = v)
        // 객관식 1번: 선택 시 바로 2단계로
        if (step == 1) {
            step = 2
        }
    }

    fun onReason(t: String) {
        form = form.copy(productivityReason = t)
        // 주관식은 여기서 step을 올리지 않는다.
        // QuestionCardText 내부 '다음' 버튼에서 onNext()를 호출.
    }

    fun onGoalAchievement(v: Int) {
        form = form.copy(goalAchievement = v)
        // 객관식 3번: 선택 시 바로 4단계로
        if (step == 3) {
            step = 4
        }
    }

    fun onNextGoal(t: String) {
        form = form.copy(nextGoal = t)
        // 마지막 주관식도 입력만 반영,
        // 제출은 QuestionCardText 내부 '제출' 버튼에서 onSubmit() 호출.
    }

    // 현재 스텝에서 '다음/제출' 버튼 활성화 여부
    val canProceed: Boolean
        get() = when (step) {
            1 -> form.productivityScore != null && (form.productivityScore ?: -1) in 0..4
            2 -> form.productivityReason.isNotBlank()
            3 -> form.goalAchievement != null && (form.goalAchievement ?: -1) in 0..4
            4 -> form.nextGoal.isNotBlank() && !isSubmitting
            else -> false
        }
    val canSubmit: Boolean
        get() =
            form.productivityScore != null &&
                    form.productivityReason.isNotBlank() &&
                    form.goalAchievement != null &&
                    form.nextGoal.isNotBlank() &&
                    !isSubmitting

    fun onPrev() {
        if (step > 1 && !isSubmitting) {
            step--
        }
    }

    /**
     * 주관식 카드(QuestionCardText)에서 '다음' 버튼이 눌렸을 때 호출.
     */
    fun onNext() {
        if (isSubmitting) return
        if (step in 1..3) {
            step++
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
        if (!canProceed || step != 4) return
        submit(onSuccess, onError)
    }

    private fun submit(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) = viewModelScope.launch {
        if (isSubmitting) return@launch
        isSubmitting = true
        try {
            val dateKey = timeProvider.now()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()

            when (val r = form.toDomain(dateKey)) {
                is Validation.Ok -> {
                    submitDailySurvey(r.value)

                    if (form.nextGoal.isNotBlank()) {
                        updateTodayTargetFromSurvey(form.nextGoal)
                    }

                    // (유스케이스 기본값 호출)
                    scheduleDailySurveyReminder()
                    onSuccess()

                    // 성공 시 초기화
                    step = 1
                    form = SurveyFormState()
                }

                is Validation.Err -> onError(r.errors)
            }
        } finally {
            isSubmitting = false
        }
    }
}
