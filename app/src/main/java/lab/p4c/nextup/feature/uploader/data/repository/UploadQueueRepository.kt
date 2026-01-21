package lab.p4c.nextup.feature.uploader.data.repository

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.upload.UploadStatus
import lab.p4c.nextup.core.domain.upload.UploadTask
import lab.p4c.nextup.core.domain.upload.UploadType
import lab.p4c.nextup.feature.uploader.data.local.dao.UploadTaskDao
import lab.p4c.nextup.feature.uploader.data.local.entity.UploadTaskEntity

@Singleton
class UploadQueueRepository @Inject constructor(
    private val dao: UploadTaskDao
) {
    /**
     * dateKey + type 는 unique 이므로 중복 enqueue는 무시된다(IGNORE).
     * - localRef: "무엇을 업로드할지" 로컬 참조(없어도 dateKey로 패키징 가능한 타입이면 null 허용)
     * - runAtMs: 바로 실행 가능이면 now, 지연 실행이면 미래 시각
     */
    suspend fun enqueue(
        type: UploadType,
        dateKey: String,
        localRef: String? = null,
        runAtMs: Long = System.currentTimeMillis(),
        priority: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        val entity = UploadTaskEntity(
            type = type.name,
            dateKey = dateKey,
            status = UploadStatus.PENDING.name,
            priority = priority,
            attempt = 0,
            nextAttemptAtMs = runAtMs,
            createdAtMs = now,
            updatedAtMs = now,
            lastError = null,
            localRef = localRef,
            remotePath = null
        )

        // IGNORE라서 이미 있으면 -1(또는 0)로 올 수 있음(Room insert 반환 규칙)
        dao.insert(entity)
    }

    /**
     * 실행 가능한 다음 작업 하나를 "RUNNING"으로 잡는다.
     * - Dao의 @Transaction popNextRunnable()을 사용해야 동시성 안전
     */
    suspend fun popNextRunnable(nowMs: Long = System.currentTimeMillis()): UploadTask? =
        withContext(Dispatchers.IO) {
            dao.popNextRunnable(nowMs)?.toDomain()
        }

    suspend fun markSuccess(
        id: Long,
        remotePath: String?
    ): Unit = withContext(Dispatchers.IO) {
        dao.markSuccess(
            id = id,
            nowMs = System.currentTimeMillis(),
            remotePath = remotePath
        )
    }

    /**
     * 실패 시 백오프(단순 단계형)
     * - Dao에서 attempt = attempt + 1 처리
     */
    suspend fun markFailed(
        id: Long,
        error: String?,
        currentAttempt: Int
    ): Unit = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val nextAttemptAtMs = now + backoffMs(currentAttempt + 1)

        dao.markFailed(
            id = id,
            nowMs = now,
            nextAttemptAtMs = nextAttemptAtMs,
            error = error?.take(500)
        )

        Log.d(
            "UploadQueue",
            "failed id=$id attempt=${currentAttempt + 1} nextAttemptAtMs=$nextAttemptAtMs"
        )
    }

    suspend fun deleteSuccessBefore(cutoffMs: Long): Int = withContext(Dispatchers.IO) {
        dao.deleteSuccessBefore(beforeMs = cutoffMs)
    }

    /**
     * 1m, 5m, 15m, 30m, 1h, 3h, 6h cap
     */
    private fun backoffMs(nextAttempt: Int): Long = when (nextAttempt) {
        1 -> 60_000L
        2 -> 5 * 60_000L
        3 -> 15 * 60_000L
        4 -> 30 * 60_000L
        5 -> 1 * 60 * 60_000L
        6 -> 3 * 60 * 60_000L
        else -> 6 * 60 * 60_000L
    }

    private fun UploadTaskEntity.toDomain(): UploadTask = UploadTask(
        id = id,
        type = UploadType.valueOf(type),
        dateKey = dateKey,
        localRef = localRef,
        status = UploadStatus.valueOf(status),
        priority = priority,
        attempt = attempt,
        nextAttemptAtMs = nextAttemptAtMs,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs,
        lastError = lastError,
        remotePath = remotePath
    )
}
