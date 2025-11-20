package lab.p4c.nextup.platform.permission

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import lab.p4c.nextup.platform.accessibility.AppAccessibilityService
import kotlin.apply
import kotlin.collections.any
import kotlin.jvm.java
import kotlin.text.equals
import kotlin.text.split

object AccessibilityPermission {

    // 접근성 서비스 활성화 여부
    fun isEnabled(ctx: Context): Boolean {
        val comp = ComponentName(ctx, AppAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(comp, ignoreCase = true) }
    }

    // 접근성 설정 열기 (API33+ 상세 페이지, 그 외 일반 설정)
    fun openSettings(ctx: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }
}
