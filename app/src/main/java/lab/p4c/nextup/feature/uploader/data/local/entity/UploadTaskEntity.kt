package lab.p4c.nextup.feature.uploader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "upload_task",
    indices = [
        Index(value = ["type", "dateKey"], unique = true),
        Index(value = ["status", "nextAttemptAtMs"])
    ]
)
data class UploadTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** USAGE | SURVEY | TELEMETRY */
    val type: String,

    /** ex) "2026-01-13" */
    val dateKey: String,

    /** PENDING | RUNNING | SUCCESS | FAILED */
    val status: String,

    /** 우선순위(높을수록 우선) */
    val priority: Int = 0,

    /** 재시도 횟수 */
    val attempt: Int = 0,

    /** 다음 실행 가능한 시각 */
    val nextAttemptAtMs: Long = 0L,

    /** 생성/갱신 */
    val createdAtMs: Long,
    val updatedAtMs: Long,

    /** 실패 시 메시지 */
    val lastError: String? = null,

    /** "무엇을 업로드할지" 로컬 참조 */
    val localRef: String? = null,

    /** 스토리지 path */
    val remotePath: String? = null
)
