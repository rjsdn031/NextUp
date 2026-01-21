package lab.p4c.nextup.core.domain.upload

data class UploadTask(
    val id: Long,
    val type: UploadType,
    val dateKey: String,

    /**
     * - USAGE: null
     * - TELEMETRY: 파일 경로/키 등
     * - SURVEY: null
     */
    val localRef: String? = null,

    val status: UploadStatus = UploadStatus.PENDING,
    val priority: Int = 0,

    val attempt: Int = 0,
    val nextAttemptAtMs: Long = 0L,

    val createdAtMs: Long,
    val updatedAtMs: Long,

    val lastError: String? = null,
    val remotePath: String? = null
)