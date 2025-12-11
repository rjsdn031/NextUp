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

                    QuestionCard(
                        question = "나는 오늘 생산적인 하루를 보냈다.",
                        options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
                        selected = form.productivityScore,
                        onSelect = vm::onProductivityScore,
                        enabled = true
                    )

                    QuestionCardText(
                        question = "그렇게 생각한 이유를 적어주세요.",
                        text = form.productivityReason,
                        enabled = step >= 2,
                        onChange = vm::onReason,
                        showNext = (step == 2),
                        onNext = vm::onNext
                    )

                    QuestionCard(
                        question = "나는 오늘 하고 싶었던 목표를 달성했다.",
                        options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
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
