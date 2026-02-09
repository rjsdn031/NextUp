package lab.p4c.nextup.feature.alarm.ui.picker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

internal object UriNameResolver {
    fun displayName(context: Context, uri: Uri): String? {
        val cr = context.contentResolver
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx < 0) return null
            if (!cursor.moveToFirst()) return null
            return cursor.getString(idx)
        }
        return null
    }
}
