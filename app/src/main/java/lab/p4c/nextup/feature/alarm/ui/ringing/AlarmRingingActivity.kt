package lab.p4c.nextup.feature.alarm.ui.ringing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lab.p4c.nextup.app.ui.theme.NextUpTheme
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.usecase.DismissAlarm
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPlayerService
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.feature.alarm.infra.scheduler.AndroidAlarmScheduler
import lab.p4c.nextup.feature.blocking.infra.BlockGate
import javax.inject.Inject

private const val SNOOZE_PREF = "alarm_snooze"
const val ACTION_BLOCK_READY_ENDED = "ACTION_BLOCK_READY_ENDED"
private fun instanceKey(id: Int) = "snooze_instance_$id"
private fun usedKey(id: Int) = "snooze_used_$id"

private fun triggeredLoggedAtKey(id: Int) = "triggered_logged_at_$id"
private const val TRIGGERED_DEDUPE_WINDOW_MS = 2_000L // 2초 내 재진입은 중복으로 간주

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject lateinit var repo: AlarmRepository
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var scheduler: AndroidAlarmScheduler
    @Inject lateinit var dismissAlarm: DismissAlarm
    @Inject lateinit var blockGate: BlockGate
    @Inject lateinit var telemetryLogger: TelemetryLogger

    private var exitHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            var alarm by remember { mutableStateOf<Alarm?>(null) }
            var isHandling by remember { mutableStateOf(false) }

            val snoozePrefs = remember {
                applicationContext.getSharedPreferences(SNOOZE_PREF, MODE_PRIVATE)
            }

            LaunchedEffect(id) {
                val a = withContext(Dispatchers.IO) { repo.getById(id) } ?: return@LaunchedEffect
                alarm = a
                title = a.name.ifBlank { "알람" }
                body = a.notificationBody

                snoozeEnabled = a.snoozeEnabled
                snoozeInterval = a.snoozeInterval
                maxSnoozeCount = a.maxSnoozeCount

                val now = System.currentTimeMillis()
                val last = snoozePrefs.getLong(triggeredLoggedAtKey(id), 0L)
                if (now - last > TRIGGERED_DEDUPE_WINDOW_MS) {

                    val used = snoozePrefs.getInt(usedKey(id), 0)
                    val isSnoozed = used > 0

                    telemetryLogger.log(
                        eventName = "AlarmTriggered",
                        payload = mapOf(
                            "AlarmId" to id.toString(),
                            "isSnoozed" to isSnoozed.toString() // "true" | "false"
                        )
                    )

                    snoozePrefs.edit { putLong(triggeredLoggedAtKey(id), now) }
                }

                if (!snoozePrefs.contains(instanceKey(id))) {
                    snoozePrefs.edit {
                        putLong(instanceKey(id), System.currentTimeMillis())
                        putInt(usedKey(id), 0)
                    }
                }

                val used = snoozePrefs.getInt(usedKey(id), 0)
                canSnooze = snoozeEnabled && (used < maxSnoozeCount)
            }

            val latestCanSnooze by rememberUpdatedState(canSnooze)
            val latestAlarm by rememberUpdatedState(alarm)
            val latestSnoozeInterval by rememberUpdatedState(snoozeInterval)
            val latestMaxSnoozeCount by rememberUpdatedState(maxSnoozeCount)

            DisposableEffect(id) {
                val callback = onBackPressedDispatcher.addCallback(this@AlarmRingingActivity) {
                    handleExit(
                        id = id,
                        canSnooze = latestCanSnooze,
                        alarm = latestAlarm,
                        snoozeInterval = latestSnoozeInterval,
                        maxSnoozeCount = latestMaxSnoozeCount,
                        snoozePrefs = snoozePrefs,
                        isHandling = isHandling,
                        setHandling = { isHandling = it },
                        setCanSnooze = { canSnooze = it }
                    )
                }
                onDispose { callback.remove() }
            }

            NextUpTheme {
                RingingScreen(
                    title = title,
                    body = body,
                    showSnooze = canSnooze,
                    snoozeMinutes = snoozeInterval,
                    onDismiss = {
                        handleDismiss(
                            id = id,
                            snoozePrefs = snoozePrefs
                        )
                    },
                    onSnooze = {
                        handleSnooze(
                            id = id,
                            alarm = alarm,
                            snoozeInterval = snoozeInterval,
                            maxSnoozeCount = maxSnoozeCount,
                            snoozePrefs = snoozePrefs,
                            setCanSnooze = { canSnooze = it },
                            isHandling = isHandling,
                            setHandling = { isHandling = it }
                        )
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id > 0 && !exitHandled) {
            exitHandled = true
            handleDismiss(id, getSharedPreferences(SNOOZE_PREF, MODE_PRIVATE))
        }
    }

    private fun handleExit(
        id: Int,
        canSnooze: Boolean,
        alarm: Alarm?,
        snoozeInterval: Int,
        maxSnoozeCount: Int,
        snoozePrefs: android.content.SharedPreferences,
        isHandling: Boolean,
        setHandling: (Boolean) -> Unit,
        setCanSnooze: (Boolean) -> Unit
    ) {
        if (exitHandled) return
        exitHandled = true

        if (canSnooze) {
            handleSnooze(
                id = id,
                alarm = alarm,
                snoozeInterval = snoozeInterval,
                maxSnoozeCount = maxSnoozeCount,
                snoozePrefs = snoozePrefs,
                setCanSnooze = setCanSnooze,
                isHandling = isHandling,
                setHandling = setHandling
            )
        } else {
            handleDismiss(id, snoozePrefs)
        }
    }

    private fun handleDismiss(id: Int, snoozePrefs: android.content.SharedPreferences) {
        telemetryLogger.log(
            eventName = "AlarmDismissed",
            payload = mapOf("AlarmId" to id.toString())
        )

        stopPlayer(id)
        snoozePrefs.edit {
            remove(instanceKey(id))
            remove(usedKey(id))
            remove(triggeredLoggedAtKey(id))
        }

        lifecycleScope.launch {
            runCatching { dismissAlarm(id) }

            blockGate.disableForMinutes(
                10,
                timeProvider.now().toEpochMilli()
            )
            startBlockReadyTimer(10)
            finish()
        }
    }

    private fun handleSnooze(
        id: Int,
        alarm: Alarm?,
        snoozeInterval: Int,
        maxSnoozeCount: Int,
        snoozePrefs: android.content.SharedPreferences,
        setCanSnooze: (Boolean) -> Unit,
        isHandling: Boolean,
        setHandling: (Boolean) -> Unit
    ) {
        if (isHandling) return
        setHandling(true)
        try {
            val used = snoozePrefs.getInt(usedKey(id), 0)
            if (used >= maxSnoozeCount) {
                setCanSnooze(false)
                handleDismiss(id, snoozePrefs)
                return
            }

            val snoozeCount = used + 1
            snoozePrefs.edit { putInt(usedKey(id), snoozeCount) }
            if (snoozeCount >= maxSnoozeCount) setCanSnooze(false)

            stopPlayer(id)

            val a = alarm ?: run {
                finish()
                return
            }

            val trigger = System.currentTimeMillis() + snoozeInterval * 60_000L
            scheduler.cancel(id)

            telemetryLogger.log(
                eventName = "AlarmSnoozed",
                payload = mapOf(
                    "AlarmId" to id.toString(),
                    "Interval" to snoozeInterval.toString(),
                    "snoozeCount" to snoozeCount.toString()
                )
            )

            scheduler.schedule(id, trigger, a)

            finish()
        } finally {
            setHandling(false)
        }
    }

    private fun stopPlayer(id: Int) {
        stopService(
            Intent(this, AlarmPlayerService::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        )
    }

    private fun startBlockReadyTimer(min: Long) {
        lifecycleScope.launch {
            kotlinx.coroutines.delay(min * 60_000L)
            sendBroadcast(Intent(ACTION_BLOCK_READY_ENDED))
        }
    }
}
