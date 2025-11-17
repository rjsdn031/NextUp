package lab.p4c.nextup.feature.blocking.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class BlockTargetStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nextup_prefs", Context.MODE_PRIVATE)

    private val KEY = "blocked_apps"

    /** 단발 조회 */
    fun get(): Set<String>? =
        prefs.getStringSet(KEY, null)?.toSet()

    /** 저장 */
    fun set(pkgs: Set<String>) {
        prefs.edit { putStringSet(KEY, pkgs.toMutableSet()) }
    }

    /** Flow 기반 observe */
    fun observe(): Flow<Set<String>?> = callbackFlow {
        trySend(get())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == KEY) {
                trySend(get())
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
