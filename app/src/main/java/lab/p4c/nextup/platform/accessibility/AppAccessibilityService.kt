package lab.p4c.nextup.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import lab.p4c.nextup.core.domain.blocking.usecase.ShouldBlockApp
import lab.p4c.nextup.core.domain.overlay.usecase.GetActiveBlockingTarget
import lab.p4c.nextup.core.domain.telemetry.port.AlarmLoggingWindow
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.blocking.infra.BlockGate
import lab.p4c.nextup.feature.blocking.infra.isBlockingActive
import lab.p4c.nextup.feature.overlay.ui.OverlayHostActivity
import lab.p4c.nextup.feature.overlay.ui.OverlayState

@AndroidEntryPoint
class AppAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var shouldBlockApp: ShouldBlockApp

    @Inject
    lateinit var getActiveBlockingTarget: GetActiveBlockingTarget

    @Inject
    lateinit var blockGate: BlockGate

    @Inject lateinit var telemetryLogger: TelemetryLogger
    @Inject lateinit var alarmLoggingWindow: AlarmLoggingWindow

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    private var lastShowMillis = 0L

    private var lastForegroundPkg: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        val appLabel = getAppLabel(pkg)

        val type = event.eventType
        val interesting =
            type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                    type == AccessibilityEvent.TYPE_VIEW_FOCUSED

        if (!interesting) return

        serviceScope.launch {
            if (pkg == packageName/* "lab.p4c.nextup" */) return@launch
            if (pkg == "com.android.systemui") return@launch

            val now = System.currentTimeMillis()

            val within = alarmLoggingWindow.isWithinWindow(now)
            if (within) {
                val prev = lastForegroundPkg
                if (prev != null && prev != pkg) {
                    val prevIsTarget = shouldBlockApp(prev, now).toString()
                    val isBlocking = blockGate.isBlockingActive(now).toString()

                    telemetryLogger.log(
                        eventName = "AppClosed",
                        payload = mapOf(
                            "isTarget" to prevIsTarget,
                            "package" to prev,
                            "isBlocking" to isBlocking
                        )
                    )
                }

                if (lastForegroundPkg != pkg) {
                    val curIsTarget = shouldBlockApp(pkg, now).toString()
                    val isBlocking = blockGate.isBlockingActive(now).toString()

                    telemetryLogger.log(
                        eventName = "AppOpened",
                        payload = mapOf(
                            "isTarget" to curIsTarget,
                            "package" to pkg,
                            "isBlocking" to isBlocking
                        )
                    )
                }
            }

            lastForegroundPkg = pkg

            val shouldBlock = shouldBlockApp(pkg, now)

            if (shouldBlock) {
                val shownRecently = now - lastShowMillis < 1500

                if (!OverlayState.isRunning || !shownRecently) {
                    val phrase = getActiveBlockingTarget()
                    val intent = Intent(this@AppAccessibilityService, OverlayHostActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("appLabel", appLabel)
                        putExtra("phrase", phrase)
                    }

                    startActivity(intent)
                    lastShowMillis = now
                }
            } else {
                // Activity 종료
            }
        }
    }

    private val overlayUnlockedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "lab.p4c.nextup.OVERLAY_UNLOCKED") {
                serviceScope.launch {
                    blockGate.disableUntilNextAlarm()
                    blockGate.clearReady()
                    Log.d(TAG, "Unlocked — blocking disabled until next alarm")
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter("lab.p4c.nextup.OVERLAY_UNLOCKED")
        registerReceiver(overlayUnlockedReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(overlayUnlockedReceiver)
        } catch (e: Exception) { }
    }

    private fun getAppLabel(pkg: String): String {
        val pm = packageManager
        return try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkg
        }
    }

    companion object {
        private const val TAG = "AppA11y"
    }
}
