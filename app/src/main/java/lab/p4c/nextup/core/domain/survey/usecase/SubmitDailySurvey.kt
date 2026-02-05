package lab.p4c.nextup.core.domain.survey.usecase

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import lab.p4c.nextup.core.domain.survey.model.DailySurvey
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.data.repository.UploadQueueRepository
import lab.p4c.nextup.feature.uploader.infra.scheduler.UploadTriggerReceiver
import javax.inject.Inject

class SubmitDailySurvey @Inject constructor(
    private val repo: SurveyRepository,
    private val uploadQueueRepository: UploadQueueRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(survey: DailySurvey) {
        repo.upsert(survey)

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
}
