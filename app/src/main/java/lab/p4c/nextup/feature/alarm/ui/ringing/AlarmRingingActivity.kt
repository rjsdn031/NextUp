package lab.p4c.nextup.feature.alarm.ui.ringing

import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import androidx.core.content.edit
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPlayerService

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject lateinit var repo: AlarmRepository
    @Inject lateinit var timeProvider: TimeProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위로/화면 켜기
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id <= 0) { finish(); return }

        // O+에서 FGS 안전 시작
        ContextCompat.startForegroundService(
            this,
            Intent(this, AlarmPlayerService::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        )

        setContent {
            var title by remember { mutableStateOf("알람") }
            var body by remember { mutableStateOf("일어날 시간입니다!") }
            var snoozeEnabled by remember { mutableStateOf(false) }
            var snoozeInterval by remember { mutableIntStateOf(5) }

            LaunchedEffect(id) {
                withContext(Dispatchers.IO) { repo.getById(id) }?.let { a ->
                    title = a.name.ifBlank { "알람" }
                    body = a.notificationBody
                    snoozeEnabled = a.snoozeEnabled
                    snoozeInterval = a.snoozeInterval
                }
            }

            RingingScreen(
                title = title,
                body = body,
                showSnooze = snoozeEnabled,
                snoozeMinutes = snoozeInterval,
                onDismiss = {
                    stopService(Intent(this, AlarmPlayerService::class.java)
                        .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id))
                    setReadyToBlock(this, timeProvider, 10)
                    finish()
                },
                onSnooze = {
                    stopService(Intent(this, AlarmPlayerService::class.java)
                        .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id))

                    val am = getSystemService(AlarmManager::class.java)
                    val trigger = System.currentTimeMillis() + snoozeInterval * 60_000L

                    val fire = PendingIntent.getService(
                        this, id,
                        Intent(this, AlarmPlayerService::class.java)
                            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val show = PendingIntent.getActivity(
                        this, id,
                        Intent(this, AlarmRingingActivity::class.java)
                            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        am.canScheduleExactAlarms()
                    }

                    am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), fire)
                    finish()
                }
            )
        }
    }
}

fun setReadyToBlock(context: Context, timeProvider: TimeProvider, min : Long) {
    val prefs = context.getSharedPreferences("nextup_prefs", Context.MODE_PRIVATE)
    val until = timeProvider.now().toEpochMilli() + (min * 60_000L)
    prefs.edit { putLong("blockReadyUntil", until) }
}