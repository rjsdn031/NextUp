package lab.p4c.nextup.feature.survey.data.repository

import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.feature.survey.data.local.SurveyDatabase
import lab.p4c.nextup.feature.survey.data.local.dao.SurveyDao
import lab.p4c.nextup.feature.survey.data.mapper.toDomain
import lab.p4c.nextup.feature.survey.data.mapper.toEntity
import javax.inject.Inject


class SurveyRepositoryImpl @Inject constructor(
    private val dao: SurveyDao
) : SurveyRepository {

    override suspend fun upsert(survey: DailySurvey) {
        dao.upsert(survey.toEntity())
    }

    override suspend fun getByDate(dateKey: String): DailySurvey? {
        return dao.getByDate(dateKey)?.toDomain()
    }
}