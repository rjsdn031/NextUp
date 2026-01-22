package lab.p4c.nextup.feature.settings.infra

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRemoteStore

class FirestoreExperimentInfoRemoteStore @Inject constructor(
    private val db: FirebaseFirestore
) : ExperimentInfoRemoteStore {

    override suspend fun upsert(uid: String, info: ExperimentInfo) {
        val payload = mapOf(
            "name" to info.name,
            "age" to info.age,
            "gender" to info.gender,
            "updatedAtMs" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("experiment")
            .document("info")
            .set(payload)
            .await()
    }
}
