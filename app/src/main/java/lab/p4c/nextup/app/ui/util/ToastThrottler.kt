package lab.p4c.nextup.app.ui.util

import android.content.Context
import android.widget.Toast
import android.os.SystemClock

class ToastThrottler(
    private val minIntervalMs: Long = 1200L
) {
    private var lastShownAtMs: Long = 0L
    private var lastMessage: String? = null
    private var toast: Toast? = null

    fun show(context: Context, message: String, force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()

        val shouldSkip =
            !force &&
                    lastMessage == message &&
                    (now - lastShownAtMs) < minIntervalMs

        if (shouldSkip) return

        lastShownAtMs = now
        lastMessage = message

        toast?.cancel()
        toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).also {
            it.show()
        }
    }
}