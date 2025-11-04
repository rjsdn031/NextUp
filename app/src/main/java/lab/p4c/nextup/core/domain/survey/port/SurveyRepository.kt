package lab.p4c.nextup.core.domain.survey.port

import lab.p4c.nextup.core.domain.survey.model.DailySurvey

interface SurveyRepository {
    suspend fun upsert(survey: DailySurvey)
    suspend fun getByDate(dateKey: String): DailySurvey?
}
