package lab.p4c.nextup.core.domain.experiment.port

import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo

interface ExperimentInfoRemoteStore {
    suspend fun upsert(uid: String, info: ExperimentInfo)
}
