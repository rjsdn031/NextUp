package lab.p4c.nextup.feature.alarm.infra.player

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.feature.alarm.ui.ringing.AlarmRingingActivity
import androidx.core.net.toUri
import kotlin.getValue

@AndroidEntryPoint
class AlarmPlayerService : Service() {

    @Inject lateinit var repo: AlarmRepository
    @Inject lateinit var ringingState: AlarmRingingState

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var originalAlarmVolume: Int? = null
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var fadeJob: Job? = null

    private var currentSessionId = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        vibrator = applicationContext.getVibratorCompat()
    }

    private fun createAlarmAudioAttributes() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private fun boostAlarmVolume() {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        originalAlarmVolume = current
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
    }

    private fun restoreAlarmVolume() {
        originalAlarmVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fadeJob?.cancel()
        player?.stop(); player?.release(); player = null
        vibrator?.cancel()

        currentSessionId++

        boostAlarmVolume()
        val sessionId = currentSessionId

        val id = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1) ?: -1
        ringingState.setRinging(true)

        startForeground(maxOf(1, id), buildNotification("알람", "일어날 시간입니다!", id))

        scope.launch {
            val alarm = withContext(Dispatchers.IO) { repo.getById(id) } ?: run { stopSelf(); return@launch }

            updateNotification(
                title = alarm.name.ifBlank { "알람" },
                body = alarm.notificationBody,
                id = id
            )

            // 재생 시작
            startPlayback(alarm, sessionId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        fadeJob?.cancel()
        player?.stop(); player?.release(); player = null
        vibrator?.cancel()
        restoreAlarmVolume()
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        ringingState.setRinging(false)
        super.onDestroy()
    }

    private fun startPlayback(alarm: Alarm, sessionId: Int) {
        // 진동
        if (alarm.vibration) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 800, 400), 0)
            )
        }

        // 사운드 (알람음 비활성화면 스킵)
        if (!alarm.alarmSoundEnabled) return

        val targetVolume = alarm.volume.toFloat().coerceIn(0f, 1f)
        val durationMs = (alarm.fadeDuration * 1000L).coerceAtLeast(0L)

        try {
            player = MediaPlayer().apply {
                setAudioAttributes(createAlarmAudioAttributes())
                setWakeMode(this@AlarmPlayerService, PARTIAL_WAKE_LOCK)
                isLooping = alarm.loopAudio

                when (val s = alarm.sound) {
                    is AlarmSound.Asset -> {
                        val resId = resources.getIdentifier(s.resName, "raw", packageName)
                        if (resId == 0) {
                            Log.e("AlarmPlayer", "Invalid asset resName=${s.resName}")
                            return
                        }
                        val uri = "android.resource://$packageName/$resId".toUri()
                        setDataSource(this@AlarmPlayerService, uri)
                    }

                    is AlarmSound.System -> {
                        val uri = s.uri.toUri()    // content://media/...
                        setDataSource(this@AlarmPlayerService, uri)
                    }

                    is AlarmSound.Custom -> {
                        val uri = s.uri.toUri()    // SAF로 선택한 파일
                        setDataSource(this@AlarmPlayerService, uri)
                    }
                }

                setVolume(0f, 0f)

                setOnPreparedListener {
                    if (sessionId != currentSessionId) return@setOnPreparedListener
                    start()
                    startFadeIn(sessionId, targetVolume, durationMs)
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AlarmPlayer", "MediaPlayer error what=$what extra=$extra")
                    true
                }

                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e("AlarmPlayer", "Sound playback failed", e)
        }
    }

    private fun startFadeIn(sessionId: Int, target: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            if (durationMs <= 0) {
                if (sessionId == currentSessionId) player?.setVolume(target, target)
                return@launch
            }
            val steps = 20
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            for (i in 1..steps) {
                if (sessionId != currentSessionId) return@launch      // 세션 바뀌면 중단
                val p = player ?: return@launch                        // 이미 정리되면 중단
                val v = target * (i / steps.toFloat())
                try {
                    p.setVolume(v, v)                                  // 준비 후 상태에서만 호출
                } catch (e: IllegalStateException) {
                    Log.w("AlarmPlayer", "setVolume in illegal state; session=$sessionId", e)
                    return@launch
                }
                delay(stepDelay)
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
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullPi, true)
            .setContentIntent(fullPi)
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
        ).apply {
            description = "알람 소리/진동 및 알림"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)
    }

    private fun Context.getVibratorCompat(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
    }

    companion object {
        const val CHANNEL_ID = "alarm_channel"
    }
}
