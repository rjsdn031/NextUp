package lab.p4c.nextup.ui.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
//import androidx.lifecycle.ViewTreeLifecycleOwner
//import androidx.savedstate.ViewTreeSavedStateRegistryOwner
//import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

object BlockingOverlayController {
    private val overlayRef = AtomicReference<WeakReference<View>?>(null)
    private val ownerRef = AtomicReference<OverlayLifecycleOwner?>(null)

    fun isShowing(): Boolean = overlayRef.get()?.get() != null

    fun show(context: Context): Boolean {
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
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val owner = OverlayLifecycleOwner()
        val view = ComposeView(appCtx).apply {
            // Compose 뷰가 창에 붙을 때/떨어질 때 라이프사이클 갱신
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) { owner.onAttach() }
                override fun onViewDetachedFromWindow(v: View) {
                    owner.onDetach()
                    overlayRef.set(null)
                    ownerRef.set(null)
                }
            })

            // Compose가 필요로 하는 오너 주입(핵심)
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)

            // detach 시 컴포지션 자동 정리
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )

            setContent { BlockingOverlayView(onDismiss = { hide(appCtx) }) }
        }

        wm.addView(view, lp)
        overlayRef.set(WeakReference(view))
        ownerRef.set(owner)
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
        }
    }
}
