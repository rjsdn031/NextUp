package lab.p4c.nextup.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

object OverlayPermission {
    fun canDraw(ctx: Context): Boolean =
        Settings.canDrawOverlays(ctx)

    fun request(ctx: Context) {
        if (canDraw(ctx)) return
        val i = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${ctx.packageName}".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }
}
