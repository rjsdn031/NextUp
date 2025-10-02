package lab.p4c.nextup.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import lab.p4c.nextup.ui.overlay.BlockingOverlayController
import lab.p4c.nextup.util.BlockedAppsStore

class AppAccessibilityService : AccessibilityService() {

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

        // 관심 이벤트만 처리 (포그라운드 전환, 창 변경 등)
        val type = event.eventType
        val interesting = type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                type == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
                type == AccessibilityEvent.TYPE_VIEW_FOCUSED
        if (!interesting) return

        val now = System.currentTimeMillis()
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0L)

        val isFromBlockedApp = blockedApps.contains(pkg)
        val isFromSystemUI = pkg.startsWith("com.android.systemui")
        val isFromSelf = pkg.startsWith(packageName)

        val shouldBlock = now < blockUntil && isFromBlockedApp

        if (shouldBlock) {
            // 너무 자주 add/remove 방지
            if (!BlockingOverlayController.isShowing() || now - lastShowMillis > 1500) {
                val shown = BlockingOverlayController.show(this)
                if (shown) {
                    lastShowMillis = now
                    Log.d(TAG, "Overlay shown for $pkg until $blockUntil")
                } else {
                    Log.d(TAG, "Overlay show skipped (no permission?)")
                }
            }
        } else {
            // 시스템 UI나 우리 앱이 전면이 아니면 숨김
            if (!isFromSystemUI && !isFromSelf && BlockingOverlayController.isShowing()) {
                BlockingOverlayController.hide(this)
                Log.d(TAG, "Overlay hidden by pkg=$pkg")
            }
        }
    }

    override fun onInterrupt() {
        // no-op
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BlockingOverlayController.isShowing()) {
            BlockingOverlayController.hide(this)
        }
    }

    companion object {
        private const val TAG = "AppA11y"
        private const val PREFS = "nextup_prefs"
        private const val KEY_BLOCK_UNTIL = "blockReadyUntil"
    }
}
