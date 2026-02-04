package lab.p4c.nextup.feature.uploader.infra.firebase

import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.platform.telemetry.user.FirebaseUserIdProvider

@Singleton
class UploadPaths @Inject constructor(
    private val userIdProvider: FirebaseUserIdProvider
) {
    private fun uid(): String =
        userIdProvider.getUserId() ?: "unknown"

    fun usage(dateKey: String): String {
        val u = uid()
        return "users/$u/usage/dateKey=$dateKey/usage_$dateKey.ndjson.gz"
    }

    // fun survey(dateKey: String): String = ...
    fun telemetry(dateKey: String): String {
        val u = uid()
        return "users/$u/telemetry/dateKey=$dateKey/telemetry.jsonl.gz"
    }
}
