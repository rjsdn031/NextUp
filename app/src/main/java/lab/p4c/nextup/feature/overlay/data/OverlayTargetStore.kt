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
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Context.overlayTargetDS by preferencesDataStore("overlay_target")

@Singleton
class OverlayTargetStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timeProvider: TimeProvider
) {
    private val fmt = DateTimeFormatter.BASIC_ISO_DATE

    private fun todayKey(): String =
        timeProvider.now().atZone(ZoneId.systemDefault()).toLocalDate().format(fmt)

    private fun prefsKey() = stringPreferencesKey("target_${todayKey()}")

    suspend fun setToday(text: String) {
        val k = prefsKey()
        context.overlayTargetDS.edit { it[k] = text }
    }

    fun observeToday(): Flow<String?> {
        val k = prefsKey()
        return context.overlayTargetDS.data.map { it[k] }
    }
}
