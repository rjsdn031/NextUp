package lab.p4c.nextup.feature.usage.infra.upload

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.infra.firebase.UploadPaths
import lab.p4c.nextup.feature.uploader.infra.runner.PreparedUpload
import lab.p4c.nextup.feature.uploader.infra.runner.UploadHandler
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository

@Singleton
class UsageUploadHandler @Inject constructor(
    private val packager: UsagePackager,
    private val usageRepository: UsageRepository,
    private val uploadPaths: UploadPaths,
) : UploadHandler {

    override val type: UploadType = UploadType.USAGE

    override suspend fun prepare(dateKey: String, localRef: String?): PreparedUpload {
        val (startMs, endMs) = parseWindow(localRef)

        val file: File = packager.buildGzipNdjsonWindow(
            dateKey = dateKey,
            startMs = startMs,
            endMs = endMs
        )

        val remotePath = uploadPaths.usage(dateKey)

        return PreparedUpload(
            type = type,
            dateKey = dateKey,
            file = file,
            remotePath = remotePath
        )
    }

    override suspend fun onUploaded(dateKey: String, localRef: String?) {
        val (startMs, endMs) = parseWindow(localRef)
        usageRepository.deleteByTimeWindow(startMs, endMs)
    }

    private fun parseWindow(localRef: String?): Pair<Long, Long> {
        require(!localRef.isNullOrBlank()) { "USAGE localRef must be 'startMs,endMs' but was=$localRef" }
        val parts = localRef.split(",")
        require(parts.size == 2) { "Invalid localRef=$localRef" }
        val startMs = parts[0].trim().toLong()
        val endMs = parts[1].trim().toLong()
        require(endMs > startMs) { "Invalid window: $startMs,$endMs" }
        return startMs to endMs
    }
}
