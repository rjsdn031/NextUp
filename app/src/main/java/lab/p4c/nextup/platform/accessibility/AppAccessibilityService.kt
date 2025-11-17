package lab.p4c.nextup.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import lab.p4c.nextup.core.domain.blocking.usecase.ShouldBlockApp
import lab.p4c.nextup.core.domain.overlay.usecase.GetActiveBlockingTarget
import lab.p4c.nextup.feature.blocking.infra.BlockGate
import lab.p4c.nextup.feature.overlay.infra.BlockingOverlayController

@AndroidEntryPoint
class AppAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var shouldBlockApp: ShouldBlockApp

    @Inject
    lateinit var getActiveBlockingTarget: GetActiveBlockingTarget

    @Inject
    lateinit var blockGate: BlockGate

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    private var lastShowMillis = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        val type = event.eventType
        val interesting =
            type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                    type == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
                    type == AccessibilityEvent.TYPE_VIEW_FOCUSED

        if (!interesting) return

        val now = System.currentTimeMillis()

        serviceScope.launch {
            val shouldBlock = shouldBlockApp(this@AppAccessibilityService, pkg)

            if (shouldBlock) {
                val shownRecently = now - lastShowMillis < 1500

                if (!BlockingOverlayController.isShowing() || !shownRecently) {
                    val phrase = getActiveBlockingTarget()

                    val shown = BlockingOverlayController.show(
                        context = this@AppAccessibilityService,
                        targetPhrase = phrase,
                        onUnlocked = {
                            blockGate.isDisabled()
                            Log.d(TAG, "Unlocked — blocking disabled until next alarm")
                        }
                    )

                    if (shown) lastShowMillis = now
                }
            } else {
                if (!isSystemUI(pkg) && !isSelf(pkg) && BlockingOverlayController.isShowing()) {
                    BlockingOverlayController.hide(this@AppAccessibilityService)
                }
            }
        }
    }

    private fun isSystemUI(pkg: String) =
        pkg.startsWith("com.android.systemui")

    private fun isSelf(pkg: String) =
        pkg.startsWith(packageName)

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
