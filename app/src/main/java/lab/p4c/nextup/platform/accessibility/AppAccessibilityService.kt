package lab.p4c.nextup.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import lab.p4c.nextup.feature.overlay.infra.BlockGate
import lab.p4c.nextup.feature.overlay.infra.BlockedAppsStore
import lab.p4c.nextup.feature.overlay.infra.BlockingOverlayController
import lab.p4c.nextup.core.domain.overlay.usecase.GetActiveBlockingTarget

@AndroidEntryPoint
class AppAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var getActiveBlockingTarget: GetActiveBlockingTarget

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    private var lastShowMillis = 0L
    private var blockedApps: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        blockedApps = BlockedAppsStore.get(this)
        Log.d(TAG, "onServiceConnected: blocked=$blockedApps")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        val type = event.eventType
        val interesting = type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                type == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
                type == AccessibilityEvent.TYPE_VIEW_FOCUSED
        if (!interesting) return

        val now = System.currentTimeMillis()

        val disabled = BlockGate.isDisabled(this)
        val isFromBlockedApp = blockedApps.contains(pkg)
        val isFromSystemUI = pkg.startsWith("com.android.systemui")
        val isFromSelf = pkg.startsWith(packageName)

        val shouldBlock = !disabled && isFromBlockedApp

        if (shouldBlock) {
            if (!BlockingOverlayController.isShowing() || now - lastShowMillis > 1500) {
                serviceScope.launch {
                    val phrase = getActiveBlockingTarget()
                    val shown = BlockingOverlayController.show(
                        context = this@AppAccessibilityService,
                        targetPhrase = phrase,
                        onUnlocked = {
                            BlockGate.disableUntilNextAlarm(this@AppAccessibilityService)
                            Log.d(
                                TAG,
                                "Overlay unlocked for $pkg — blocking disabled until next alarm"
                            )
                        }
                    )
                    if (shown) lastShowMillis = now
                }
            }
        } else {
            if (!isFromSystemUI && !isFromSelf && BlockingOverlayController.isShowing()) {
                BlockingOverlayController.hide(this)
                Log.d(TAG, "Overlay hidden by pkg=$pkg")
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (BlockingOverlayController.isShowing()) {
            BlockingOverlayController.hide(this)
        }
    }

    companion object {
        private const val TAG = "AppA11y"
    }
}
