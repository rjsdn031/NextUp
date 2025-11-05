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

    // 1~4 단계
    var step by mutableIntStateOf(1)
        private set

    // 폼 입력 상태
    var form by mutableStateOf(SurveyFormState())
        private set

    // 중복 제출 방지
    var isSubmitting by mutableStateOf(false)
        private set

    // 입력 핸들러
    fun onProductivityScore(v: Int) { form = form.copy(productivityScore = v) }
    fun onReason(t: String)          { form = form.copy(productivityReason = t) }
    fun onGoalAchievement(v: Int)    { form = form.copy(goalAchievement = v) }
    fun onNextGoal(t: String)        { form = form.copy(nextGoal = t) }

    // 현재 스텝에서 '다음/완료' 버튼 활성화 여부
    val canProceed: Boolean
        get() = when (step) {
            1 -> form.productivityScore != null && (form.productivityScore ?: -1) in 0..4
            2 -> form.productivityReason.isNotBlank()
            3 -> form.goalAchievement != null && (form.goalAchievement ?: -1) in 0..4
            4 -> form.nextGoal.isNotBlank() && !isSubmitting
            else -> false
        }

    fun onPrev() { if (step > 1 && !isSubmitting) step-- }

    fun onNextOrSubmit(
        onSuccess: () -> Unit,
        onError: (List<SurveyValidationError>) -> Unit
    ) {
        if (step < 4) {
            step++
            return
        }
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

                    // 매일 21:00 (유스케이스 기본값 호출)
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
