package lab.p4c.nextup.feature.uploader.infra.runner

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.upload.UploadTask
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.data.repository.UploadQueueRepository
import lab.p4c.nextup.feature.uploader.infra.firebase.FirebaseStorageUploader

@Singleton
class UploadRunner @Inject constructor(
    private val queue: UploadQueueRepository,
    private val storage: FirebaseStorageUploader,
    handlers: Set<@JvmSuppressWildcards UploadHandler>, // Hilt multibinding
) {
    private val handlerMap: Map<UploadType, UploadHandler> =
        handlers.associateBy { it.type }

    private val runMutex = Mutex()

    /**
     * 큐를 가능한 만큼 비움.
     * - maxItems: 한 번에 너무 길게 돌지 않도록 제한
     */
    suspend fun drain(maxItems: Int = 20) = runMutex.withLock {
        withContext(Dispatchers.IO) {
            var processed = 0
            while (processed < maxItems) {
                val task = queue.popNextRunnable() ?: break
                val ok = runOne(task)
                processed++
                Log.d("UploadRunner", "runOne done id=${task.id} ok=$ok processed=$processed")
            }
            Log.d("UploadRunner", "drain finished processed=$processed")
        }
    }

    private suspend fun runOne(task: UploadTask): Boolean {
        val handler = handlerMap[task.type]
        if (handler == null) {
            queue.markFailed(
                id = task.id,
                error = "No handler for type=${task.type}",
                currentAttempt = task.attempt
            )
            return false
        }

        return runCatching {
            // 1) payload 준비 (파일 생성 + remotePath 결정)
            val prepared = handler.prepare(task.dateKey, task.localRef)

            // 2) 업로드 (Firebase Storage)
            val remotePath = storage.uploadFile(prepared.remotePath, prepared.file)

            // 3) 큐 성공 처리
            queue.markSuccess(task.id, remotePath)

            // 4) 후처리(로컬 정리 등)
            runCatching {
                handler.onUploaded(task.dateKey, task.localRef)

                runCatching { prepared.file.delete() }
            }.onFailure { t ->
                Log.e(
                    "UploadRunner",
                    "post-upload cleanup failed id=${task.id} type=${task.type} dateKey=${task.dateKey}",
                    t
                )
            }

            true
        }.getOrElse { t ->
            val msg = (t.message ?: t::class.java.simpleName).take(500)
            Log.e(
                "UploadRunner",
                "upload failed id=${task.id} type=${task.type} dateKey=${task.dateKey} err=$msg",
                t
            )
            queue.markFailed(
                id = task.id,
                error = msg,
                currentAttempt = task.attempt
            )
            false
        }
    }
}
