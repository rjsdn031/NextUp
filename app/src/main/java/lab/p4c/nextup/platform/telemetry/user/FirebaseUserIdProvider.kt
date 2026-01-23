package lab.p4c.nextup.platform.telemetry.user

import lab.p4c.nextup.core.domain.auth.port.AuthClient
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.UserIdProvider

@Singleton
class FirebaseUserIdProvider @Inject constructor(
    private val authClient: AuthClient
) : UserIdProvider {
    override fun getUserId(): String? = authClient.currentUidOrNull()
}