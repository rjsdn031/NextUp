// util/BlockGate.kt
package lab.p4c.nextup.util

import android.content.Context
import androidx.core.content.edit

object BlockGate {
    private const val PREFS = "nextup_prefs"
    private const val KEY = "blockDisabledUntilNextAlarm"

    fun isDisabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY, false)

    fun disableUntilNextAlarm(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY, true) }
    }

    fun rearmForNextAlarm(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY, false) }
    }
}
