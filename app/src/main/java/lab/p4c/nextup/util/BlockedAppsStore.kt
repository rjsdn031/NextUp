package lab.p4c.nextup.util

import android.content.Context
import androidx.core.content.edit

object BlockedAppsStore {
    private const val PREFS = "nextup_prefs"
    private const val KEY_SET = "blocked_apps"

    private val DEFAULT = setOf("com.google.android.youtube")

    fun get(ctx: Context): Set<String> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_SET, null)
            ?.toSet() ?: DEFAULT

    fun set(ctx: Context, pkgs: Set<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putStringSet(KEY_SET, pkgs.toMutableSet()) }
    }
}
