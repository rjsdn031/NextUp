package lab.p4c.nextup.core.domain.auth.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.auth.port.AuthClient

class EnsureAnonymousSignedIn @Inject constructor(
    private val auth: AuthClient
) {
    suspend operator fun invoke(): String {
        return auth.currentUidOrNull() ?: auth.ensureSignedIn()
    }
}
