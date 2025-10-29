package lab.p4c.nextup.feature.alarm.ui.ringing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPlayerService
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.feature.alarm.infra.scheduler.AndroidAlarmScheduler
import javax.inject.Inject

private const val SNOOZE_PREF = "alarm_snooze"
private fun instanceKey(id: Int) = "snooze_instance_$id"
private fun usedKey(id: Int) = "snooze_used_$id"


@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject
    lateinit var repo: AlarmRepository
    @Inject
    lateinit var timeProvider: TimeProvider
    @Inject
    lateinit var scheduler: AndroidAlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위로/화면 켜기
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id <= 0) {
            finish(); return
        }

        setContent {
            var title by remember { mutableStateOf("알람") }
            var body by remember { mutableStateOf("일어날 시간입니다!") }
            var snoozeEnabled by remember { mutableStateOf(false) }
            var snoozeInterval by remember { mutableIntStateOf(5) }
            var maxSnoozeCount by remember { mutableIntStateOf(3) }
            var canSnooze by remember { mutableStateOf(false) }
            var isSnoozing by remember { mutableStateOf(false) }

            var alarm by remember { mutableStateOf<Alarm?>(null) }
            val snoozePrefs = remember {
                applicationContext.getSharedPreferences(
                    SNOOZE_PREF,
                    MODE_PRIVATE
                )
            }

            LaunchedEffect(id) {
                val a = withContext(Dispatchers.IO) { repo.getById(id) } ?: return@LaunchedEffect
                alarm = a
                title = a.name.ifBlank { "알람" }
                body = a.notificationBody
                snoozeEnabled = a.snoozeEnabled
                snoozeInterval = a.snoozeInterval
                maxSnoozeCount = a.maxSnoozeCount

                if (!snoozePrefs.contains(instanceKey(id))) {
                    snoozePrefs.edit {
                        putLong(instanceKey(id), System.currentTimeMillis())
                        putInt(usedKey(id), 0)
                    }
                }

                val used = snoozePrefs.getInt(usedKey(id), 0)
                canSnooze = snoozeEnabled && (used < maxSnoozeCount)
            }

            RingingScreen(
                title = title,
                body = body,
                showSnooze = canSnooze,
                snoozeMinutes = snoozeInterval,
                onDismiss = {
                    stopService(
                        Intent(this, AlarmPlayerService::class.java)
                            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
                    )

                    snoozePrefs.edit {
                        remove(instanceKey(id))
                        remove(usedKey(id))
                    }

                    setReadyToBlock(this, timeProvider, 10)
                    finish()
                },

                onSnooze = {
                    if (isSnoozing) return@RingingScreen
                    isSnoozing = true
                    try {
                        val used = snoozePrefs.getInt(usedKey(id), 0)
                        if (used >= maxSnoozeCount) {
                            canSnooze = false
                            return@RingingScreen
                        }

                        snoozePrefs.edit { putInt(usedKey(id), used + 1) }
                        val nextUsed = used + 1
                        if (nextUsed >= maxSnoozeCount) {
                            canSnooze = false
                        }

                        stopService(
                            Intent(this, AlarmPlayerService::class.java)
                                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
                        )

                        val a = alarm ?: return@RingingScreen
                        val trigger = System.currentTimeMillis() + snoozeInterval * 60_000L

                        scheduler.cancel(id)
                        scheduler.schedule(id, trigger, a)

                        finish()
                    } finally {
                        isSnoozing = false
                    }
                }
            )
        }
    }
}

fun setReadyToBlock(context: Context, timeProvider: TimeProvider, min: Long) {
    val prefs = context.getSharedPreferences("nextup_prefs", Context.MODE_PRIVATE)
    val until = timeProvider.now().toEpochMilli() + (min * 60_000L)
    prefs.edit { putLong("blockReadyUntil", until) }
}