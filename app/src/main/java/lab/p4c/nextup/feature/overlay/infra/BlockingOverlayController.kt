package lab.p4c.nextup.feature.overlay.infra

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import lab.p4c.nextup.feature.overlay.ui.BlockingOverlayView
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase

object BlockingOverlayController {
    private val overlayRef = AtomicReference<WeakReference<View>?>(null)
    private val ownerRef = AtomicReference<OverlayLifecycleOwner?>(null)

    // UI 업데이트 바인딩용 콜백 홀더
    private val setTargetRef = AtomicReference<((String) -> Unit)?>(null)
    private val setPartialRef = AtomicReference<((String, Float) -> Unit)?>(null)
    private val setPhaseRef = AtomicReference<((UnlockPhase) -> Unit)?>(null)

    // STT 세션
    private var stt: SpeechUnlockSession? = null



    fun isShowing(): Boolean = overlayRef.get()?.get() != null

    /**
     * @param targetPhrase 따라 말할 문장(숫자 포함 권장)
     * @param onUnlocked 성공 시 호출(차단 해제 후 후속 처리)
     */
    fun show(context: Context, targetPhrase: String, onUnlocked: () -> Unit): Boolean {
        if (isShowing()) return true
        val appCtx = context.applicationContext
        val wm = appCtx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            // WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val owner = OverlayLifecycleOwner()
        val view = ComposeView(appCtx).apply {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    owner.onAttach()
                }

                override fun onViewDetachedFromWindow(v: View) {
                    owner.onDetach()
                    overlayRef.set(null)
                    ownerRef.set(null)
                    clearBindings()
                    stopSession()
                }
            })

            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)

            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )

            // onBind를 통해 Compose <-> Controller 양방향 바인딩
            setContent {
                BlockingOverlayView(
                    onDismiss = { hide(appCtx) },
                    onStartListening = { startSession(appCtx, targetPhrase, onUnlocked) },
                    onStopListening = { stopSession() },
                    onConfirm = { hide(context); onUnlocked() },
                    onBind = { setTarget, setPhase, setPartial ->
                        setTargetRef.set(setTarget)
                        setPartialRef.set(setPartial)
                        setPhaseRef.set { phase ->
                            postMain { setPhase(phase) }
                        }

                        setTarget(targetPhrase)
                        setPhase(UnlockPhase.Idle)
                    }
                )
            }
        }

        wm.addView(view, lp)
        overlayRef.set(WeakReference(view))
        ownerRef.set(owner)

        return true
    }

    fun hide(context: Context) {
        val view = overlayRef.get()?.get() ?: return
        val wm =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.removeView(view)
        } catch (_: Throwable) {
        } finally {
            ownerRef.getAndSet(null)?.onDetach()
            overlayRef.set(null)

            clearBindings()
            stopSession()
        }
    }

    private fun clearBindings() {
        setTargetRef.set(null)
        setPartialRef.set(null)
        setPhaseRef.set(null)
    }

    // ==== STT 세션 제어 ====
    private fun startSession(
        context: Context,
        targetPhrase: String,
        onUnlocked: () -> Unit
    ) {
        stopSession()
        stt = SpeechUnlockSession(
            context = context,
            targetPhrase = targetPhrase,
            onPhase = { phase -> setPhaseRef.get()?.invoke(phase) },
            onPartial = { hyp, sim ->
                setPartialRef.get()?.let { postMain { it(hyp, sim) } }
            },
            onSuccess = { /* 성공 시 UI 상태는 Matched에서 처리됨 */ },
            onErrorUi = { /* 필요시 로깅 전용 */ }
        ).also { it.start() }
    }

    private fun stopSession() {
        stt?.stop()
        stt = null
    }

    private fun postMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else Handler(Looper.getMainLooper()).post(block)
    }
}
