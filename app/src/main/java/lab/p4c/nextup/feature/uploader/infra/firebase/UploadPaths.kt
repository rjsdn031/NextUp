package lab.p4c.nextup.feature.uploader.infra.firebase

import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.UserIdProvider

@Singleton
class UploadPaths @Inject constructor(
    private val userIdProvider: UserIdProvider
) {
    private fun uid(): String =
        userIdProvider.getUserId().trim().ifEmpty { "unknown" }

    fun usage(dateKey: String): String {
        // users/{uid}/usage/dateKey=YYYY-MM-DD/usage_YYYY-MM-DD.ndjson.gz
        val u = uid()
        return "users/$u/usage/dateKey=$dateKey/usage_$dateKey.ndjson.gz"
    }

    // fun survey(dateKey: String): String = ...
    // fun telemetry(dateKey: String): String = ...
}
