package lab.p4c.nextup.core.domain.survey.usecase

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRemoteStore
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.data.repository.UploadQueueRepository
import lab.p4c.nextup.feature.uploader.infra.scheduler.UploadTriggerReceiver
import lab.p4c.nextup.platform.telemetry.user.FirebaseUserIdProvider

class SubmitDailySurvey @Inject constructor(
    private val repo: SurveyRepository,
    private val remoteStore: SurveyRemoteStore,
    private val userIdProvider: FirebaseUserIdProvider,
    private val uploadQueueRepository: UploadQueueRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(survey: DailySurvey) {
        repo.upsert(survey)

        val uid = userIdProvider.getUserId()?.trim().orEmpty()
        if (uid.isNotEmpty()) {
            // TODO:
            // If authentication can fail in future flows,
            // consider adding retry
            val uploaded = retry(
                times = 3,
                initialDelayMs = 300,
                maxDelayMs = 3_000,
            ) {
                remoteStore.upsert(uid, survey)
            }

            if (uploaded) return
        }

        uploadQueueRepository.enqueue(
            type = UploadType.SURVEY,
            dateKey = survey.dateKey,
            localRef = null,
            runAtMs = System.currentTimeMillis(),
            priority = 20
        )

        val appCtx = context.applicationContext
        appCtx.sendBroadcast(
            Intent(appCtx, UploadTriggerReceiver::class.java)
                .setAction(UploadTriggerReceiver.UPLOAD_DAILY)
        )
    }

    private suspend fun retry(
        times: Int,
        initialDelayMs: Long,
        maxDelayMs: Long,
        block: suspend () -> Unit
    ): Boolean {
        var delayMs = initialDelayMs
        repeat(times) { attempt ->
            try {
                block()
                return true
            } catch (_: Throwable) {
                if (attempt == times - 1) return false
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
            }
        }
        return false
    }
}