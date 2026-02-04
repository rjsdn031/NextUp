package lab.p4c.nextup.feature.telemetry.infra.upload

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.telemetry.data.repository.TelemetryRepository
import lab.p4c.nextup.feature.uploader.infra.firebase.UploadPaths
import lab.p4c.nextup.feature.uploader.infra.runner.PreparedUpload
import lab.p4c.nextup.feature.uploader.infra.runner.UploadHandler

@Singleton
class TelemetryUploadHandler @Inject constructor(
    private val packager: TelemetryPackager,
    private val telemetryRepository: TelemetryRepository,
    private val uploadPaths: UploadPaths,
) : UploadHandler {

    override val type: UploadType = UploadType.TELEMETRY

    override suspend fun prepare(dateKey: String, localRef: String?): PreparedUpload {
        val file: File = packager.buildGzipJsonlForDate(dateKey)
        val remotePath = uploadPaths.telemetry(dateKey)

        return PreparedUpload(
            type = type,
            dateKey = dateKey,
            file = file,
            remotePath = remotePath
        )
    }

    override suspend fun onUploaded(dateKey: String, localRef: String?) {
        telemetryRepository.deleteDateFile(dateKey)
    }
}
