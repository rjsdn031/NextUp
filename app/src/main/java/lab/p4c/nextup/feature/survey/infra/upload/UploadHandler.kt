package lab.p4c.nextup.feature.survey.infra.upload

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.survey.data.local.dao.SurveyDao
import lab.p4c.nextup.feature.uploader.infra.firebase.UploadPaths
import lab.p4c.nextup.feature.uploader.infra.runner.PreparedUpload
import lab.p4c.nextup.feature.uploader.infra.runner.UploadHandler

@Singleton
class SurveyUploadHandler @Inject constructor(
    private val dao: SurveyDao,
    private val fileBuilder: SurveyUploadFileBuilder,
    private val uploadPaths: UploadPaths
) : UploadHandler {

    override val type: UploadType = UploadType.SURVEY

    override suspend fun prepare(
        dateKey: String,
        localRef: String?
    ): PreparedUpload {
        val file: File = fileBuilder.build(dateKey)
        val remotePath = uploadPaths.survey(dateKey)

        return PreparedUpload(
            type = type,
            dateKey = dateKey,
            file = file,
            remotePath = remotePath
        )
    }

    override suspend fun onUploaded(dateKey: String, localRef: String?) {
        // 후처리 없음
    }
}
