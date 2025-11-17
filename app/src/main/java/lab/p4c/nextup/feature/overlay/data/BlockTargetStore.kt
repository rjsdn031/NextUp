package lab.p4c.nextup.feature.overlay.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.blockTargetDS by preferencesDataStore("block_target")

@Singleton
class BlockTargetStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val KEY = stringPreferencesKey("block_targets")

    fun observe(): Flow<Set<String>> =
        context.blockTargetDS.data.map { prefs ->
            prefs[KEY]?.split(",")?.toSet() ?: emptySet()
        }

    suspend fun set(targets: Set<String>) {
        val csv = targets.joinToString(",")
        context.blockTargetDS.edit { it[KEY] = csv }
    }
}