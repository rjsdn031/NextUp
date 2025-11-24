package lab.p4c.nextup.core.domain.experiment.port

import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo

interface ExperimentInfoRepository {
    suspend fun save(info: ExperimentInfo)
    suspend fun get(): ExperimentInfo?
}