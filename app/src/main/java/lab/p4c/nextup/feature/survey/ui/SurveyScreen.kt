package lab.p4c.nextup.feature.survey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.survey.ui.components.QuestionCard
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardText

@Composable
fun SurveyScreen(
    viewModel: SurveyScreenViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onError: (List<SurveyValidationError>) -> Unit = {}
) {
    val step by viewModel::step
    val form by viewModel::form
    val canProceed = viewModel.canProceed

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            when (step) {
                1 -> QuestionCard(
                    question = "오늘 얼마나 생산적이라고 느꼈나요?",
                    options = listOf("0", "1", "2", "3", "4"),
                    selected = form.productivityScore,
                    onSelect = viewModel::onProductivityScore
                )
                2 -> QuestionCardText(
                    question = "왜 그렇게 생각하나요?",
                    text = form.productivityReason,
                    onChange = viewModel::onReason
                )
                3 -> QuestionCard(
                    question = "오늘 하고 싶은 목표를 달성했나요?",
                    options = listOf("0", "1", "2", "3", "4"),
                    selected = form.goalAchievement,
                    onSelect = viewModel::onGoalAchievement
                )
                4 -> QuestionCardText(
                    question = "내일 이루고 싶은 목표가 무엇인가요?",
                    text = form.nextGoal,
                    onChange = viewModel::onNextGoal
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (step > 1) {
                    TextButton(onClick = viewModel::onPrev) {
                        Text("이전")
                    }
                } else {
                    Spacer(Modifier.width(64.dp))
                }

                Button(
                    onClick = {
                        viewModel.onNextOrSubmit(
                            onSuccess = onComplete,
                            onError = onError
                        )
                    },
                    enabled = canProceed
                ) {
                    Text(if (step < 4) "다음" else "완료")
                }
            }
        }
    }
}
