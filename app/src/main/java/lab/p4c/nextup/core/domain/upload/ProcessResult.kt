package lab.p4c.nextup.core.domain.upload

sealed interface ProcessResult {
    data class Success(val remotePath: String?) : ProcessResult
    data class Fail(val error: String?) : ProcessResult
}
