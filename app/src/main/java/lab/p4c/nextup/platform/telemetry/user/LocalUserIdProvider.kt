package lab.p4c.nextup.platform.telemetry.user

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.UserIdProvider
import androidx.core.content.edit

@Singleton
class LocalUserIdProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : UserIdProvider {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getUserId(): String {
        val existing = prefs.getString(KEY_USER_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val created = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_USER_ID, created) }
        return created
    }

    private companion object {
        private const val PREFS_NAME = "telemetry"
        private const val KEY_USER_ID = "telemetry_user_id"
    }
}
