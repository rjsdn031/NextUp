package lab.p4c.nextup.core.domain.overlay.usecase

import lab.p4c.nextup.core.domain.overlay.model.TargetSource
import javax.inject.Inject
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository

class UpdateTodayTargetFromSurvey @Inject constructor(
    private val repo: OverlayTargetRepository
) {
    suspend operator fun invoke(text: String) {
        if (text.isNotBlank()) repo.setToday(text, TargetSource.SURVEY)
    }
}
