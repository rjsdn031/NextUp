package lab.p4c.nextup.core.domain.survey.port

import lab.p4c.nextup.core.domain.survey.model.DailySurvey

interface SurveyRemoteStore {
    suspend fun upsert(uid: String, survey: DailySurvey)
}