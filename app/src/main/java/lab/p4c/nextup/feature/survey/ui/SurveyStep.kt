package lab.p4c.nextup.feature.survey.ui

/**
 * Defines the ordered steps of the daily survey flow.
 *
 * The enum order represents the logical progression of the survey.
 * Step navigation in [SurveyScreenViewModel] depends on this order.
 *
 * Notes:
 * - [MissedReason] is conditionally included depending on whether
 *   yesterday's survey was missing.
 * - The remaining steps form the core survey flow.
 *
 * If a new step is added:
 * - It must be inserted in the correct logical position.
 * - [SurveyScreenViewModel.baseFlow] should be updated accordingly.
 * - UI rendering in SurveyStepContent must be extended.
 */
enum class SurveyStep {
    MissedReason,
    SleepTime,
    SleepQuality,
    ProductivityScore,
//    ProductivityReason,
    GoalAchievement,
    NextGoal
}