package lab.p4c.nextup.feature.survey.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.survey.ui.components.QuestionCard
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardText
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardTimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    vm: SurveyScreenViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onError: (List<SurveyValidationError>) -> Unit = {}
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val step = vm.step
    val form = vm.form
    val needsMissedReason = vm.needsMissedReason

    val scroll = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
//            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "오늘의 설문",
                            style = t.titleLarge,
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = c.background,
                        titleContentColor = c.onBackground
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scroll)
                        .padding(top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(48.dp)
                ) {

                    if (needsMissedReason) {
                        QuestionCardText(
                            question = "어제 설문을 하지 못한 이유가 무엇인가요?",
                            text = form.missedYesterdayReason,
                            placeholder = "5자 이상 적어주세요",
                            enabled = step >= 1,
                            onChange = vm::onMissedYesterdayReason,
                            showNext = (step == 1),
                            onNext = vm::onNext
                        )
                    }

                    QuestionCardTimeRange(
                        question = "어제 잠든 시간과 오늘 깬 시간을 입력해주세요.",
                        startTime = form.sleepStartTime,
                        endTime = form.sleepEndTime,
                        enabled = step >= 2,
                        onStartChange = vm::onSleepStartTime,
                        onEndChange = vm::onSleepEndTime,
                        showNext = (step == 2),
                        onNext = vm::onNext
                    )

                    QuestionCard(
                        question = "어젯밤 수면의 질은 어땠나요?",
                        options = listOf("매우 나쁨", "나쁨", "보통", "좋음", "매우 좋음"),
                        selected = form.sleepQualityScore,
                        onSelect = vm::onSleepQualityScore,
                        enabled = step >= 3
                    )

                    QuestionCard(
                        question = "오늘 생산적인 하루를 보냈다고 생각하나요?",
                        options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
                        selected = form.productivityScore,
                        onSelect = vm::onProductivityScore,
                        enabled = step >= 4
                    )

                    QuestionCardText(
                        question = "그렇게 생각한 이유를 적어주세요.",
                        text = form.productivityReason,
                        enabled = step >= 5,
                        onChange = vm::onReason,
                        showNext = (step == 5),
                        onNext = vm::onNext
                    )

                    QuestionCard(
                        question = "오늘 하고 싶었던 목표를 달성했나요?",
                        options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
                        selected = form.goalAchievement,
                        enabled = step >= 6,
                        onSelect = vm::onGoalAchievement
                    )

                    QuestionCardText(
                        question = "내일 이루고 싶은 목표가 무엇인가요?",
                        text = form.nextGoal,
                        placeholder = "간단히 적어주세요",
                        enabled = step >= 7,
                        onChange = vm::onNextGoal,
                        showSubmit = true,
                        enabledSubmit = vm.canSubmit,
                        onSubmit = { vm.onSubmit(onComplete, onError) }
                    )

                }
            }
        }
    }
}
