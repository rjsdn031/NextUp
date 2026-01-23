package lab.p4c.nextup.core.domain.auth.port

interface AuthClient {
    fun currentUidOrNull(): String?
    suspend fun ensureSignedIn(): String
}
