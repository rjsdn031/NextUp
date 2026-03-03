package lab.p4c.nextup.feature.settings.infra

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRemoteStore

class FirestoreExperimentInfoRemoteStore @Inject constructor(
    private val db: FirebaseFirestore
) : ExperimentInfoRemoteStore {

    override suspend fun upsert(uid: String, info: ExperimentInfo) {
        val now = System.currentTimeMillis()
        val userRef = db.collection("users").document(uid)

        db.runTransaction { tx ->
            val snap = tx.get(userRef)

            if (!snap.exists()) {
                tx.set(
                    userRef,
                    mapOf(
                        "createdAtMs" to now,
                        "updatedAtMs" to now,
                        "isActive" to true
                    )
                )
            } else {
                tx.set(
                    userRef,
                    mapOf(
                        "updatedAtMs" to now,
                        "isActive" to true
                    ),
                    SetOptions.merge()
                )
            }
        }.await()

        val infoPayload = mapOf(
            "name" to info.name,
            "age" to info.age,
            "gender" to info.gender,
            "updatedAtMs" to now
        )

        userRef
            .collection("experiment")
            .document("info")
            .set(infoPayload, SetOptions.merge())
            .await()
    }
}