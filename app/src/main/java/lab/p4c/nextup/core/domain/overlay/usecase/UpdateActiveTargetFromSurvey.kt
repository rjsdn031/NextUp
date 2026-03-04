package lab.p4c.nextup.core.domain.overlay.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.overlay.model.TargetSource
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository

/**
 * Updates the active overlay goal using the goal text from a survey submission.
 */
class UpdateActiveGoalFromSurvey @Inject constructor(
    private val repo: OverlayTargetRepository
) {
    /**
     * @param goalText The user-written goal text from [lab.p4c.nextup.core.domain.survey.model.DailySurvey.nextGoal].
     */
    suspend operator fun invoke(goalText: String) {
        val normalized = goalText.trim()
        if (normalized.isNotEmpty()) {
            repo.setActiveGoal(normalized, TargetSource.SURVEY)
        }
    }
}