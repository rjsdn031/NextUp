package lab.p4c.nextup.util

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri

object BatteryOptimizationPermission {

    /** 현재 앱이 배터리 최적화 예외인지 여부 */
    fun isIgnoring(ctx: Context): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    /** 일반 '배터리 최적화' 설정 목록 열기 (요청 아님, 단순 이동) */
    fun openOptimizationSettings(ctx: Context) {
        val i = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }
            .onFailure {
                // 폴백: 앱 상세 화면
                val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData("package:${ctx.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(details)
            }
    }
}
