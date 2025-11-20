package lab.p4c.nextup.feature.survey.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.survey.ui.components.QuestionCard
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardText

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

    val scroll = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
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

                    QuestionCard(
                        question = "오늘 얼마나 생산적이라고 느꼈나요?",
                        options = listOf("전혀 생산적이지 않았다", "다소 생산적이지 않았다", "그저 그렇다", "대체로 생산적이었다", "매우 생산적이었다"),
                        selected = form.productivityScore,
                        onSelect = vm::onProductivityScore,
                        enabled = true
                    )

                    QuestionCardText(
                        question = "왜 그렇게 생각하나요?",
                        text = form.productivityReason,
                        enabled = step >= 2,
                        onChange = vm::onReason,
                        showNext = (step == 2),
                        onNext = vm::onNext
                    )

                    QuestionCard(
                        question = "오늘 하고 싶은 목표를 달성했나요?",
                        options = listOf("전혀 달성하지 못했다", "일부만 달성했다", "절반 정도 달성했다", "대부분 달성했다", "모두 달성했다"),
                        selected = form.goalAchievement,
                        enabled = step >= 3,
                        onSelect = vm::onGoalAchievement
                    )

                    QuestionCardText(
                        question = "내일 이루고 싶은 목표가 무엇인가요?",
                        text = form.nextGoal,
                        enabled = step >= 4,
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
