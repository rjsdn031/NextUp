package lab.p4c.nextup.feature.overlay.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.overlayTargetDS by preferencesDataStore("overlay_target")

/**
 * Persists the active goal text used by the blocking overlay.
 *
 * The active goal is not scoped by date. This avoids unintended expiration at midnight.
 * The value stays effective until overwritten by a new survey submission (or explicitly cleared).
 */
@Singleton
class OverlayTargetStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyActiveText = stringPreferencesKey("active_goal_text")
    private val keyUpdatedAtMs = longPreferencesKey("active_goal_updated_at_ms")

    /**
     * Saves the active goal text.
     *
     * @param text Active goal text to be shown by the overlay.
     * @param updatedAtMs UTC epoch millis when the goal was updated.
     */
    suspend fun setActiveGoal(text: String, updatedAtMs: Long = System.currentTimeMillis()) {
        context.overlayTargetDS.edit { prefs ->
            prefs[keyActiveText] = text
            prefs[keyUpdatedAtMs] = updatedAtMs
        }
    }

    /**
     * Observes the active goal text.
     *
     * This store does not apply any default fallback.
     * Use a repository to provide a default when the value is missing or blank.
     */
    fun observeActiveGoalText(): Flow<String?> {
        return context.overlayTargetDS.data.map { prefs ->
            prefs[keyActiveText]
        }
    }

    /**
     * Clears the active goal text explicitly.
     */
    suspend fun clearActiveGoal() {
        context.overlayTargetDS.edit { prefs ->
            prefs.remove(keyActiveText)
            prefs.remove(keyUpdatedAtMs)
        }
    }

    // TODO: Optional cleanup of legacy date-keyed entries if they existed in previous versions.
}