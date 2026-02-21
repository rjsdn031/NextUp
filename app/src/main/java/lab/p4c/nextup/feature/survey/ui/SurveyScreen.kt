package lab.p4c.nextup.feature.survey.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.survey.ui.components.QuestionCard
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardText
import lab.p4c.nextup.feature.survey.ui.components.QuestionCardTimeRange
import lab.p4c.nextup.feature.survey.ui.components.SurveyNavBar

/**
 * Composable entry point for the daily survey screen.
 *
 * Design principles:
 * - Single-step (one question per screen) wizard-style flow.
 * - No automatic step progression; navigation is controlled exclusively
 *   via the bottom CTA (Previous / Next / Submit).
 * - Horizontal slide animation is applied when the step changes.
 * - The bottom navigation bar uses `imePadding()` so that it remains visible
 *   when the software keyboard is shown.
 *
 * This screen is purely state-driven and relies entirely on
 * [SurveyScreenViewModel] for navigation logic and validation.
 *
 * @param vm ViewModel managing survey state and submission logic.
 * @param onComplete Callback invoked when the survey is successfully submitted.
 * @param onError Callback invoked when submission fails due to validation errors.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    val focusManager = LocalFocusManager.current

    var prevStep by remember { mutableStateOf(step) }
    val forward = step.ordinal > prevStep.ordinal
    LaunchedEffect(step) { prevStep = step }

    val scrollState = rememberScrollState()
    LaunchedEffect(step) { scrollState.scrollTo(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("오늘의 설문", style = t.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.background,
                    titleContentColor = c.onBackground
                )
            )
        },
        bottomBar = {
            SurveyNavBar(
                showPrev = !vm.isFirstStep(),
                onPrev = {
                    focusManager.clearFocus()
                    vm.goPrev()
                },
                primaryText = vm.primaryText(),
                primaryEnabled = vm.primaryEnabled(),
                onPrimary = {
                    focusManager.clearFocus()
                    vm.onPrimary(onComplete, onError)
                },
                modifier = Modifier.imePadding()
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val direction = if (forward) 1 else -1
                slideInHorizontally { it * direction } + fadeIn() togetherWith
                        slideOutHorizontally { -it * direction } + fadeOut()
            },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 24.dp)
        ) { s ->
            SurveyStepContent(
                step = s,
                needsMissedReason = needsMissedReason,
                form = form,
                vm = vm
            )
        }
    }
}

/**
 * Renders the content corresponding to the current [SurveyStep].
 *
 * - Exhaustive `when` branching ensures compile-time safety
 *   when steps are added or modified.
 * - This composable is responsible only for UI rendering.
 * - All state mutations are delegated to [SurveyScreenViewModel].
 *
 * @param step Current survey step.
 * @param needsMissedReason Whether the "missed yesterday reason" step is required.
 * @param form Current survey form state.
 * @param vm ViewModel providing update callbacks.
 */
@Composable
private fun SurveyStepContent(
    step: SurveyStep,
    needsMissedReason: Boolean,
    form: SurveyFormState,
    vm: SurveyScreenViewModel,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (step) {
            SurveyStep.MissedReason -> {
                if (needsMissedReason) {
                    QuestionCardText(
                        question = "어제 설문을 하지 못한 이유가 무엇인가요?",
                        text = form.missedYesterdayReason,
                        placeholder = "10자 이상 적어주세요",
                        enabled = true,
                        onChange = vm::onMissedYesterdayReason
                    )
                }
            }

            SurveyStep.SleepTime -> {
                QuestionCardTimeRange(
                    question = "어제 잠든 시간과 오늘 깬 시간을 입력해주세요.",
                    startTime = form.sleepStartTime,
                    endTime = form.sleepEndTime,
                    enabled = true,
                    onStartChange = vm::onSleepStartTime,
                    onEndChange = vm::onSleepEndTime
                )
            }

            SurveyStep.SleepQuality -> {
                QuestionCard(
                    question = "어젯밤 수면의 질은 어땠나요?",
                    options = listOf("매우 나쁨", "나쁨", "보통", "좋음", "매우 좋음"),
                    selected = form.sleepQualityScore,
                    onSelect = vm::onSleepQualityScore,
                    enabled = true
                )
            }

            SurveyStep.ProductivityScore -> {
                QuestionCard(
                    question = "오늘 생산적인 하루를 보냈다고 생각하나요?",
                    options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
                    selected = form.productivityScore,
                    onSelect = vm::onProductivityScore,
                    enabled = true
                )
            }

            SurveyStep.ProductivityReason -> {
                QuestionCardText(
                    question = "그렇게 생각한 이유를 적어주세요.",
                    text = form.productivityReason,
                    placeholder = "10자 이상 적어주세요",
                    enabled = true,
                    onChange = vm::onReason
                )
            }

            SurveyStep.GoalAchievement -> {
                QuestionCard(
                    question = "오늘 하고 싶었던 목표를 달성했나요?",
                    options = listOf("매우 그렇지 않다", "그렇지 않다", "보통이다", "그렇다", "매우 그렇다"),
                    selected = form.goalAchievement,
                    onSelect = vm::onGoalAchievement,
                    enabled = true
                )
            }

            SurveyStep.NextGoal -> {
                QuestionCardText(
                    question = "내일 이루고 싶은 목표가 무엇인가요?",
                    text = form.nextGoal,
                    placeholder = "10자 이상 적어주세요",
                    enabled = true,
                    onChange = vm::onNextGoal
                )
            }
        }
    }
}