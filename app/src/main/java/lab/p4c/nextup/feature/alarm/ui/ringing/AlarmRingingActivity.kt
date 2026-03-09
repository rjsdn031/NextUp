package lab.p4c.nextup.feature.alarm.ui.ringing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import lab.p4c.nextup.app.ui.theme.NextUpTheme
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.usecase.DismissAlarm
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.core.domain.telemetry.port.AlarmLoggingWindow
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPlayerService
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.feature.alarm.infra.scheduler.AndroidAlarmScheduler
import lab.p4c.nextup.feature.blocking.infra.BlockGate

private const val SNOOZE_PREF = "alarm_snooze"
const val ACTION_BLOCK_READY_ENDED = "ACTION_BLOCK_READY_ENDED"
private const val TRIGGERED_DEDUPE_WINDOW_MS = 2_000L

private fun instanceKey(id: Int) = "snooze_instance_$id"
private fun usedKey(id: Int) = "snooze_used_$id"
private fun triggeredLoggedAtKey(id: Int) = "triggered_logged_at_$id"

private fun Alarm.effectiveSnoozeEnabled(): Boolean =
    if (id == 1) true else snoozeEnabled

private fun Alarm.effectiveSnoozeInterval(): Int =
    if (id == 1) 5 else snoozeInterval

private fun Alarm.effectiveMaxSnoozeCount(): Int =
    if (id == 1) Int.MAX_VALUE else maxSnoozeCount

private enum class ExitAction {
    DISMISS,
    TIMEOUT,
    SNOOZE,
}

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject lateinit var repo: AlarmRepository
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var scheduler: AndroidAlarmScheduler
    @Inject lateinit var dismissAlarm: DismissAlarm
    @Inject lateinit var blockGate: BlockGate
    @Inject lateinit var telemetryLogger: TelemetryLogger
    @Inject lateinit var alarmLoggingWindow: AlarmLoggingWindow

    private var exitHandled = false
    private var timeoutReceiverRegistered = false

    private var currentAlarm: Alarm? = null
    private var currentCanSnooze = false
    private var currentSnoozeInterval = 5
    private var currentMaxSnoozeCount = 3
    private var currentIsHandling = false

    private var exitProcessingStarted = false
    private var pendingExitAction: ExitAction? = null

    private val snoozePrefs by lazy {
        applicationContext.getSharedPreferences(SNOOZE_PREF, MODE_PRIVATE)
    }

    private val timeoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("AlarmRinging", "timeoutReceiver onReceive intent=$intent")
            if (intent?.action != AlarmPlayerService.ACTION_RING_TIMEOUT) return

            val targetId = intent.getIntExtra(AlarmPlayerService.EXTRA_ALARM_ID, -1)
            if (targetId <= 0) return

            val currentId = this@AlarmRingingActivity.intent
                .getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)

            if (targetId != currentId) return

            requestExitAction(targetId, ExitAction.TIMEOUT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id <= 0) {
            finish()
            return
        }

        setContent {
            var title by remember { mutableStateOf("알람") }
            var body by remember { mutableStateOf("일어날 시간입니다!") }

            var snoozeInterval by remember { mutableIntStateOf(5) }
            var maxSnoozeCount by remember { mutableIntStateOf(3) }
            var canSnooze by remember { mutableStateOf(false) }

            var alarm by remember { mutableStateOf<Alarm?>(null) }
            var isHandling by remember { mutableStateOf(false) }

            LaunchedEffect(id) {
                val loadedAlarm = withContext(Dispatchers.IO) { repo.getById(id) } ?: return@LaunchedEffect
                alarm = loadedAlarm
                currentAlarm = loadedAlarm

                title = loadedAlarm.name.ifBlank { "알람" }
                body = loadedAlarm.notificationBody

                val effectiveSnoozeInterval = loadedAlarm.effectiveSnoozeInterval()
                val effectiveMaxSnoozeCount = loadedAlarm.effectiveMaxSnoozeCount()

                snoozeInterval = effectiveSnoozeInterval
                maxSnoozeCount = effectiveMaxSnoozeCount

                currentSnoozeInterval = effectiveSnoozeInterval
                currentMaxSnoozeCount = effectiveMaxSnoozeCount

                val now = System.currentTimeMillis()
                val last = snoozePrefs.getLong(triggeredLoggedAtKey(id), 0L)
                if (now - last > TRIGGERED_DEDUPE_WINDOW_MS) {
                    val used = snoozePrefs.getInt(usedKey(id), 0)
                    val isSnoozed = used > 0

                    alarmLoggingWindow.markAlarmTriggered(timeProvider.now().toEpochMilli())
                    telemetryLogger.log(
                        eventName = "AlarmTriggered",
                        payload = mapOf(
                            "AlarmId" to id.toString(),
                            "isSnoozed" to isSnoozed.toString()
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
                val computedCanSnooze =
                    loadedAlarm.effectiveSnoozeEnabled() && (used < effectiveMaxSnoozeCount)

                canSnooze = computedCanSnooze
                currentCanSnooze = computedCanSnooze
            }

            DisposableEffect(id) {
                val callback = onBackPressedDispatcher.addCallback(this@AlarmRingingActivity) {
                    requestDefaultExit(id)
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
                        requestExitAction(id, ExitAction.DISMISS)
                    },
                    onSnooze = {
                        requestExitAction(id, ExitAction.SNOOZE)
                    }
                )
            }

            LaunchedEffect(alarm, canSnooze, snoozeInterval, maxSnoozeCount, isHandling) {
                currentAlarm = alarm
                currentCanSnooze = canSnooze
                currentSnoozeInterval = snoozeInterval
                currentMaxSnoozeCount = maxSnoozeCount
                currentIsHandling = isHandling
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val filter = android.content.IntentFilter(
            AlarmPlayerService.ACTION_RING_TIMEOUT
        )

        ContextCompat.registerReceiver(
            this,
            timeoutReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        timeoutReceiverRegistered = true
    }

    override fun onStop() {
        if (timeoutReceiverRegistered) {
            runCatching { unregisterReceiver(timeoutReceiver) }
            timeoutReceiverRegistered = false
        }

        if (shouldHandleSystemExitOnStop()) {
            val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
            if (id > 0) {
                requestDefaultExit(id)
            }
        }

        super.onStop()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id > 0) {
            requestDefaultExit(id)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        if (id <= 0) return super.onKeyDown(keyCode, event)

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_ASSIST,
            KeyEvent.KEYCODE_VOICE_ASSIST -> {
                requestDefaultExit(id)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun shouldHandleSystemExitOnStop(): Boolean {
        return !exitHandled &&
                !isFinishing &&
                !isChangingConfigurations
    }

    private fun requestDefaultExit(id: Int) {
        val action = if (currentCanSnooze) ExitAction.SNOOZE else ExitAction.DISMISS
        requestExitAction(id, action)
    }

    private fun requestExitAction(id: Int, incomingAction: ExitAction) {
        if (exitHandled) return

        pendingExitAction = chooseHigherPriorityAction(
            current = pendingExitAction,
            incoming = incomingAction
        )

        if (exitProcessingStarted) return
        exitProcessingStarted = true

        lifecycleScope.launch {
            yield()

            when (pendingExitAction ?: ExitAction.DISMISS) {
                ExitAction.SNOOZE -> handleSnoozeInternal(id)
                ExitAction.TIMEOUT -> handleTimeoutInternal(id)
                ExitAction.DISMISS -> handleDismissInternal(id)
            }
        }
    }

    private fun chooseHigherPriorityAction(
        current: ExitAction?,
        incoming: ExitAction
    ): ExitAction {
        val currentPriority = priorityOf(current)
        val incomingPriority = priorityOf(incoming)
        return if (incomingPriority > currentPriority) incoming else current ?: incoming
    }

    private fun priorityOf(action: ExitAction?): Int {
        return when (action) {
            ExitAction.DISMISS -> 0
            ExitAction.TIMEOUT -> 1
            ExitAction.SNOOZE -> 2
            null -> -1
        }
    }

    private fun tryMarkExitHandled(): Boolean {
        if (exitHandled) return false
        exitHandled = true
        return true
    }

    private fun handleDismissInternal(id: Int) {
        if (!tryMarkExitHandled()) return

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

    private fun handleSnoozeInternal(id: Int) {
        if (!tryMarkExitHandled()) return
        if (currentIsHandling) return

        currentIsHandling = true
        try {
            val used = snoozePrefs.getInt(usedKey(id), 0)
            if (used >= currentMaxSnoozeCount) {
                currentCanSnooze = false
                handleDismissInternal(id)
                return
            }

            val snoozeCount = used + 1
            snoozePrefs.edit { putInt(usedKey(id), snoozeCount) }

            if (snoozeCount >= currentMaxSnoozeCount) {
                currentCanSnooze = false
            }

            stopPlayer(id)

            val alarm = currentAlarm ?: run {
                finish()
                return
            }

            val trigger = System.currentTimeMillis() + currentSnoozeInterval * 60_000L
            scheduler.cancel(id)

            telemetryLogger.log(
                eventName = "AlarmSnoozed",
                payload = mapOf(
                    "AlarmId" to id.toString(),
                    "Interval" to currentSnoozeInterval.toString(),
                    "snoozeCount" to snoozeCount.toString()
                )
            )

            scheduler.schedule(id, trigger, alarm)
            finish()
        } finally {
            currentIsHandling = false
        }
    }

    private fun handleTimeoutInternal(id: Int) {
        if (!tryMarkExitHandled()) return

        stopPlayer(id)

        lifecycleScope.launch {
            val alarm = withContext(Dispatchers.IO) { repo.getById(id) }

            if (alarm == null) {
                handleTimeoutDismissInternal(id)
                return@launch
            }

            val used = snoozePrefs.getInt(usedKey(id), 0)
            val effectiveSnoozeEnabled = alarm.effectiveSnoozeEnabled()
            val effectiveMaxSnoozeCount = alarm.effectiveMaxSnoozeCount()
            val canAutoSnooze = effectiveSnoozeEnabled && used < effectiveMaxSnoozeCount

            if (canAutoSnooze) {
                handleAutoSnoozeInternal(
                    id = id,
                    alarm = alarm,
                    used = used
                )
            } else {
                handleTimeoutDismissInternal(id)
            }
        }
    }

    private fun handleTimeoutDismissInternal(id: Int) {
        telemetryLogger.log(
            eventName = "AlarmTimedOut",
            payload = mapOf("AlarmId" to id.toString())
        )

        snoozePrefs.edit {
            remove(instanceKey(id))
            remove(usedKey(id))
            remove(triggeredLoggedAtKey(id))
        }

        lifecycleScope.launch {
            blockGate.disableForMinutes(10, timeProvider.now().toEpochMilli())
            startBlockReadyTimer(10)
            finish()
        }
    }

    private fun handleAutoSnoozeInternal(
        id: Int,
        alarm: Alarm,
        used: Int
    ) {
        val nextCount = used + 1
        snoozePrefs.edit { putInt(usedKey(id), nextCount) }

        val effectiveSnoozeInterval = alarm.effectiveSnoozeInterval()
        val trigger = System.currentTimeMillis() + effectiveSnoozeInterval * 60_000L

        scheduler.cancel(id)

        telemetryLogger.log(
            eventName = "AlarmAutoSnoozed",
            payload = mapOf(
                "AlarmId" to id.toString(),
                "Interval" to effectiveSnoozeInterval.toString(),
                "snoozeCount" to nextCount.toString()
            )
        )

        scheduler.schedule(id, trigger, alarm)
        finish()
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