package lab.p4c.nextup.feature.overlay.infra

import android.app.Activity
import android.util.Log
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase

object BlockingOverlayController {

    private var stt: SpeechUnlockSession? = null

    private var setTarget: ((String) -> Unit)? = null
    private var setPhase: ((UnlockPhase) -> Unit)? = null
    private var setPartial: ((String, Float) -> Unit)? = null
    private var setErrno: ((Int?) -> Unit)? = null

    fun bind(
        setTarget: (String) -> Unit,
        setPhase: (UnlockPhase) -> Unit,
        setPartial: (String, Float) -> Unit,
        setErrno: (Int?) -> Unit,
        targetPhrase: String,
    ) {
        this.setTarget = setTarget
        this.setPhase = setPhase
        this.setPartial = setPartial
        this.setErrno = setErrno

        setTarget(targetPhrase)
        setErrno(null)
        setPhase(UnlockPhase.Idle)
    }

    fun startSession(
        activity: Activity,
        targetPhrase: String,
        onUnlocked: () -> Unit,
        onFailed: (UnlockPhase) -> Unit,
    ) {
        stopSession(clearBindings = false)
        setErrno?.invoke(null)

        stt = SpeechUnlockSession(
            context = activity.applicationContext,
            targetPhrase = targetPhrase,
            onPhase = { phase ->
                setPhase?.invoke(phase)
                if (phase.isTerminalFailure()) onFailed(phase)
            },
            onPartial = { hyp, sim ->
                setPartial?.invoke(hyp, sim)
            },
            onSuccess = {
                setErrno?.invoke(null)
                setPhase?.invoke(UnlockPhase.Matched)
                onUnlocked()
            },
            onErrorUi = { code ->
                Log.e("STT", "SpeechRecognizer error: $code")
                setErrno?.invoke(code)
            }
        ).also { it.start() }
    }

    fun stopSession(clearBindings: Boolean = true) {
        stt?.stop()
        stt = null
        setErrno?.invoke(null)

        if (clearBindings) {
            setTarget = null
            setPhase = null
            setPartial = null
            setErrno = null
        }
    }
}

private fun UnlockPhase.isTerminalFailure(): Boolean = when (this) {
    UnlockPhase.Mismatch,
    UnlockPhase.Timeout,
    UnlockPhase.Busy,
    UnlockPhase.PermissionErr,
    UnlockPhase.ClientErr -> true
    else -> false
}