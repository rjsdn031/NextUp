package lab.p4c.nextup.feature.uploader.infra.runner

import java.io.File
import lab.p4c.nextup.core.domain.upload.UploadType

data class PreparedUpload(
    val type: UploadType,
    val dateKey: String,
    val file: File,
    val remotePath: String
)

/**
 * 각 타입(USAGE/SURVEY/TELEMETRY)이
 * - dateKey 기준으로 업로드할 파일을 만들고
 * - 업로드 경로(remotePath)를 결정하고
 * - 성공 후 로컬 정리를 수행한다
 */
interface UploadHandler {
    val type: UploadType
    suspend fun prepare(dateKey: String, localRef: String?): PreparedUpload
    suspend fun onUploaded(dateKey: String, localRef: String?)
}