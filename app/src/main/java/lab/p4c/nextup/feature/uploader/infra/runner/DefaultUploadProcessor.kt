package lab.p4c.nextup.feature.uploader.infra.runner

import lab.p4c.nextup.core.domain.upload.ProcessResult
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.upload.UploadTask
import lab.p4c.nextup.feature.uploader.infra.firebase.FirebaseStorageUploader

@Singleton
class DefaultUploadProcessor @Inject constructor(
    handlers: Set<@JvmSuppressWildcards UploadHandler>,
    private val storageUploader: FirebaseStorageUploader
) {
    private val handlerMap = handlers.associateBy { it.type }

    suspend fun process(task: UploadTask): ProcessResult {
        val handler = handlerMap[task.type]
            ?: return ProcessResult.Fail("No handler for type=${task.type}")

        return runCatching {
            val prepared = handler.prepare(task.dateKey, task.localRef)

            storageUploader.uploadFile(
                remotePath = prepared.remotePath,
                file = prepared.file
            )

            handler.onUploaded(task.dateKey, task.localRef)

            ProcessResult.Success(prepared.remotePath)
        }.getOrElse { t ->
            ProcessResult.Fail(t.message ?: "upload failed")
        }
    }
}
