package lab.p4c.nextup.feature.uploader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import lab.p4c.nextup.core.domain.upload.UploadStatus
import lab.p4c.nextup.feature.uploader.data.local.entity.UploadTaskEntity

@Dao
interface UploadTaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: UploadTaskEntity): Long

    @Query(
        """
        SELECT * FROM upload_task
        WHERE (status = :pending OR status = :failed)
          AND nextAttemptAtMs <= :nowMs
        ORDER BY priority DESC, createdAtMs ASC
        LIMIT 1
    """
    )
    suspend fun nextRunnable(
        nowMs: Long,
        pending: UploadStatus = UploadStatus.PENDING,
        failed: UploadStatus = UploadStatus.FAILED
    ): UploadTaskEntity?

    @Query(
        """
    UPDATE upload_task
    SET status = :running,
        updatedAtMs = :nowMs
    WHERE id = :id
      AND status IN (:pending, :failed)
"""
    )
    suspend fun markRunning(
        id: Long,
        nowMs: Long,
        running: UploadStatus = UploadStatus.RUNNING,
        pending: UploadStatus = UploadStatus.PENDING,
        failed: UploadStatus = UploadStatus.FAILED
    ): Int

    @Query(
        """
        UPDATE upload_task
        SET status = :success,
            updatedAtMs = :nowMs,
            remotePath = :remotePath,
            lastError = NULL
        WHERE id = :id
    """
    )
    suspend fun markSuccess(
        id: Long,
        nowMs: Long,
        remotePath: String?,
        success: UploadStatus = UploadStatus.SUCCESS
    ): Int

    /** 실패 표시 + 백오프 */
    @Query(
        """
        UPDATE upload_task
        SET status = :failed,
            updatedAtMs = :nowMs,
            attempt = attempt + 1,
            nextAttemptAtMs = :nextAttemptAtMs,
            lastError = :error
        WHERE id = :id
    """
    )
    suspend fun markFailed(
        id: Long,
        nowMs: Long,
        nextAttemptAtMs: Long,
        error: String?,
        failed: UploadStatus = UploadStatus.FAILED
    ): Int

    /** 성공 후 태스크 정리 */
    @Query(
        """
        DELETE FROM upload_task
        WHERE status = :success
          AND updatedAtMs < :beforeMs
    """
    )
    suspend fun deleteSuccessBefore(
        beforeMs: Long,
        success: UploadStatus = UploadStatus.SUCCESS
    ): Int

    /**
     * "가져오기 + 러닝 마킹" 묶기
     */
    @Transaction
    suspend fun popNextRunnable(nowMs: Long): UploadTaskEntity? {
        val next = nextRunnable(nowMs) ?: return null
        val updated = markRunning(next.id, nowMs)
        return if (updated == 1) next.copy(status = UploadStatus.RUNNING.name) else null
    }
}
