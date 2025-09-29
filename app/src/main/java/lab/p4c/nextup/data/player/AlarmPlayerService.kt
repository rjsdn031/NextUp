package lab.p4c.nextup.data.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import lab.p4c.nextup.data.scheduler.AlarmReceiver
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.domain.repository.AlarmRepository
import lab.p4c.nextup.ui.screen.ringing.AlarmRingingActivity

@AndroidEntryPoint
class AlarmPlayerService : Service() {

    @Inject lateinit var repo: AlarmRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var fadeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1) ?: -1

        startForeground(maxOf(1, id), buildNotification("알람", "일어날 시간입니다!", id))

        scope.launch {
            val alarm = withContext(Dispatchers.IO) { repo.getById(id) }
            if (alarm == null) {
                stopSelf()
                return@launch
            }

            // 알림 업데이트
            updateNotification(
                title = alarm.name.ifBlank { "알람" },
                body = alarm.notificationBody,
                id = id
            )

            // 재생 시작
            startPlayback(alarm)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        fadeJob?.cancel()
        player?.stop(); player?.release(); player = null
        vibrator?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun startPlayback(alarm: Alarm) {
        // 진동
        if (alarm.vibration) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 800, 400), 0)
            )
        }

        // 사운드 (알람음 비활성화면 스킵)
        if (alarm.alarmSoundEnabled) {
            try {
                val pathInAssets = alarm.assetAudioPath.removePrefix("assets/")
                val afd = assets.openFd(pathInAssets)
                player = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    isLooping = alarm.loopAudio
                    setVolume(0f, 0f)
                    prepare()
                    start()
                }

                // 페이드 인
                val target = alarm.volume.toFloat().coerceIn(0f, 1f)
                val durationMs = (alarm.fadeDuration * 1000L).coerceAtLeast(0)
                fadeJob?.cancel()
                fadeJob = scope.launch {
                    if (durationMs <= 0) {
                        player?.setVolume(target, target)
                    } else {
                        val steps = 20
                        val stepDelay = durationMs / steps
                        for (i in 1..steps) {
                            val v = target * (i / steps.toFloat())
                            player?.setVolume(v, v)
                            delay(stepDelay)
                        }
                    }
                }
            } catch (_: Exception) {
                // 에셋 문제 등으로 실패해도 서비스는 계속 유지(진동/풀스크린 화면)
            }
        }
    }

    private fun buildNotification(title: String, body: String, id: Int): Notification {
        val full = Intent(this, AlarmRingingActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)

        val fullPi = PendingIntent.getActivity(
            this, id, full,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(fullPi, true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, body: String, id: Int) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, buildNotification(title, body, id))
    }

    private fun createChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC }
        nm.createNotificationChannel(ch)
    }

    companion object {
        const val CHANNEL_ID = "alarm_channel"
    }
}
