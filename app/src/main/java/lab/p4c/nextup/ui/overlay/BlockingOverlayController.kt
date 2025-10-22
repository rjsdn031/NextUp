package lab.p4c.nextup.ui.overlay

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

object BlockingOverlayController {
    private val overlayRef = AtomicReference<WeakReference<View>?>(null)
    private val ownerRef = AtomicReference<OverlayLifecycleOwner?>(null)

    // UI 업데이트 바인딩용 콜백 홀더
    private val setTargetRef = AtomicReference<((String) -> Unit)?>(null)
    private val setStateRef = AtomicReference<((String) -> Unit)?>(null)
    private val setPartialRef = AtomicReference<((String, Float) -> Unit)?>(null)

    // STT 세션
    private var stt: SpeechUnlockSession? = null

    private var lastTarget: String? = null
    private var lastState: String? = null
    private var lastPartial: Pair<String, Float>? = null

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
                override fun onViewAttachedToWindow(v: View) { owner.onAttach() }
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
                    onConfirm = {
                        hide(context)
                        onUnlocked()
                                },
                    onBind = { setTarget, setState, setPartial ->
                        setTargetRef.set(setTarget)
                        setStateRef.set(setState)
                        setPartialRef.set(setPartial)
                        lastTarget?.let { setTarget(it) }
                        lastState?.let { setState(it) }
                        lastPartial?.let { (h, s) -> setPartial(h, s) }
                    }
                )
            }
        }

        wm.addView(view, lp)
        overlayRef.set(WeakReference(view))
        ownerRef.set(owner)

        // Compose가 올라오도록 한 프레임 뒤에 UI 초기값 세팅 & STT 시작
        Handler(Looper.getMainLooper()).post {
            lastTarget = targetPhrase
            lastState = "대기 중…"
            setTargetRef.get()?.invoke(targetPhrase)
            setStateRef.get()?.invoke("버튼을 눌러 말하기를 시작하세요")
        }

        return true
    }

    fun hide(context: Context) {
        val view = overlayRef.get()?.get() ?: return
        val wm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.removeView(view)
        } catch (_: Throwable) {
        } finally {
            ownerRef.getAndSet(null)?.onDetach()
            overlayRef.set(null)

            lastTarget = null
            lastState = null
            lastPartial = null

            clearBindings()
            stopSession()
        }
    }

    private fun clearBindings() {
        setTargetRef.set(null)
        setStateRef.set(null)
        setPartialRef.set(null)
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
            onState = { text ->
                lastState = text
                setStateRef.get()?.let { postMain { it(text) } }
            },
            onPartial = { hyp, sim ->
                lastPartial = hyp to sim
                setPartialRef.get()?.let { postMain { it(hyp, sim) } }
            },
            onSuccess = {
                setStateRef.get()?.let { postMain { it("충분히 인식됨. ‘이용하기’를 눌러 계속하세요") } }
            },
            onErrorUi = { code ->
                when (code) {
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        setStateRef.get()?.let { postMain { it("다시 말해주세요") } }
                    }
                    else -> {
                        setStateRef.get()?.let { postMain { it("인식 오류($code)") } }
                    }
                }
            }

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
