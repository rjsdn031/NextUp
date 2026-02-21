package lab.p4c.nextup.feature.survey.infra

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRemoteStore

/**
 * Firestore implementation of [SurveyRemoteStore].
 *
 * Data layout:
 * - users/{uid}/survey/{dateKey}
 *
 * Note:
 * - The caller is responsible for providing a valid [uid].
 * - Authentication failure handling is intentionally deferred (TODO),
 *   because the app flow collects experiment info and signs in earlier.
 */
class FirestoreSurveyRemoteStore @Inject constructor(
    private val db: FirebaseFirestore
) : SurveyRemoteStore {

    override suspend fun upsert(uid: String, survey: DailySurvey) {
        val payload = mapOf(
            "dateKey" to survey.dateKey,
            "missedYesterdayReason" to survey.missedYesterdayReason,
            "sleepStartTime" to survey.sleepStartTime,
            "sleepEndTime" to survey.sleepEndTime,
            "sleepQualityScore" to survey.sleepQualityScore,
            "productivityScore" to survey.productivityScore,
            "productivityReason" to survey.productivityReason,
            "goalAchievement" to survey.goalAchievement,
            "nextGoal" to survey.nextGoal,
            "updatedAtMs" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection(COLLECTION_SURVEY)
            .document(survey.dateKey)
            .set(payload)
            .await()
    }

    private companion object {
        const val COLLECTION_SURVEY = "survey"
    }
}