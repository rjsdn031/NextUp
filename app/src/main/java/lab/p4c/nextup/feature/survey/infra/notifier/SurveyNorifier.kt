package lab.p4c.nextup.feature.survey.infra.notifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import lab.p4c.nextup.R
import androidx.core.net.toUri

private const val CHANNEL_ID = "survey"
private const val NOTI_ID = 2001

class SurveyNotifier(private val context: Context) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyDailySurvey() {
        ensureChannel()

        // Compose NavGraph는 XML이 없으므로 Intent + URI 딥링크로 진입
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "app://nextup/survey?source=notif".toUri()
            setPackage(context.packageName) // 우리 앱으로만 라우트
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // 여기는 실제 존재하는 아이콘으로 바꿔줘
            // 예) R.drawable.ic_notification_small 또는 R.mipmap.ic_launcher
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("하루 설문을 작성해주세요")
            .setContentText("1분이면 끝납니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTI_ID, notification)
    }

    private fun ensureChannel() {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = mgr.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Survey",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily survey reminders"
        }
        mgr.createNotificationChannel(ch)
    }
}
