package lab.p4c.nextup.feature.blocking.infra

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockGate @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("nextup_prefs", Context.MODE_PRIVATE)
    }

    fun isDisabled(): Boolean =
        prefs.getBoolean(KEY, false)

    fun disableUntilNextAlarm() {
        prefs.edit { putBoolean(KEY, true) }
    }

    fun rearmForNextAlarm() {
        prefs.edit { putBoolean(KEY, false) }
    }

    companion object {
        private const val KEY = "blockDisabledUntilNextAlarm"
    }
}