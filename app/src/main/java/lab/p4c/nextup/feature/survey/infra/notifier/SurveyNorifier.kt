package lab.p4c.nextup.feature.survey.infra.notifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import lab.p4c.nextup.R

private const val CHANNEL_ID = "survey"
private const val NOTI_ID = 2001

class SurveyNotifier(private val context: Context) {

    /**
     * 알림을 표시합니다. 권한이 없으면 아무 것도 하지 않고 false를 반환합니다.
     */
    fun notifyDailySurvey(): Boolean {
        // Android 13+ 알림 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false
            }
        }

        ensureChannel()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "app://nextup/survey?source=notif".toUri()
            setPackage(context.packageName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("데일리 설문을 작성해주세요")
            .setContentText("3분이면 충분합니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTI_ID, notification)
        return true
    }

    private fun ensureChannel() {
        val mgr = context.getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        mgr.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Survey",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Daily survey reminders" }
        )
    }
}
