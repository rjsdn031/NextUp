package lab.p4c.nextup.core.domain.survey.usecase

import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import javax.inject.Inject

class SubmitDailySurvey @Inject constructor(
    private val repo: SurveyRepository
) {
    suspend operator fun invoke(s: DailySurvey) {
        repo.upsert(s)
    }
}
