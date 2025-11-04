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
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.survey.usecase.SubmitDailySurvey
import java.time.ZoneId

@HiltViewModel
class SurveyScreenViewModel @Inject constructor(
    private val submitDailySurvey: SubmitDailySurvey,
    private val scheduleDailySurveyReminder: ScheduleDailySurveyReminder,
    private val timeProvider: TimeProvider
) : ViewModel() {

    // 1~4 단계
    var step by mutableIntStateOf(1)
        private set

    // 폼 입력 상태 (불완전 가능)
    var form by mutableStateOf(SurveyFormState())
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
            4 -> form.nextGoal.isNotBlank()
            else -> false
        }

    fun onPrev() { if (step > 1) step-- }

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
        val dateKey = timeProvider.now()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        when (val r = form.toDomain(dateKey)) {
            is Validation.Ok -> {
                submitDailySurvey(r.value)
                scheduleDailySurveyReminder(21, 0)
                onSuccess()
            }
            is Validation.Err -> {
                onError(r.errors)
            }
        }
    }
}
