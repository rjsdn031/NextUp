package lab.p4c.nextup.core.domain.experiment.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRemoteStore

class UpsertExperimentInfoRemote @Inject constructor(
    private val remote: ExperimentInfoRemoteStore
) {
    suspend operator fun invoke(uid: String, info: ExperimentInfo) {
        remote.upsert(uid, info)
    }
}
