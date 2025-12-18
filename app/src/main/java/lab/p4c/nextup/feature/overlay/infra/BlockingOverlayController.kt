package lab.p4c.nextup.feature.overlay.infra

import android.app.Activity
import android.util.Log
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase

object BlockingOverlayController {

    private var stt: SpeechUnlockSession? = null

    private var setTarget: ((String) -> Unit)? = null
    private var setPhase: ((UnlockPhase) -> Unit)? = null
    private var setPartial: ((String, Float) -> Unit)? = null

    fun bind(
        setTarget: (String) -> Unit,
        setPhase: (UnlockPhase) -> Unit,
        setPartial: (String, Float) -> Unit,
        targetPhrase: String
    ) {
        this.setTarget = setTarget
        this.setPhase = setPhase
        this.setPartial = setPartial

        setTarget(targetPhrase)
        setPhase(UnlockPhase.Idle)
    }

    fun startSession(
        activity: Activity,
        targetPhrase: String,
        onUnlocked: () -> Unit,
        onFailed: (UnlockPhase) -> Unit
    ) {
        stopSession()

        stt = SpeechUnlockSession(
            context = activity,
            targetPhrase = targetPhrase,
            onPhase = { phase ->
                setPhase?.invoke(phase)
                if (phase.isTerminalFailure()) onFailed(phase)
            },
            onPartial = { hyp, sim ->
                setPartial?.invoke(hyp, sim)
            },
            onSuccess = {
                setPhase?.invoke(UnlockPhase.Matched)
                onUnlocked()
            },
            onErrorUi = { code ->
                Log.e("STT", "SpeechRecognizer error: $code")
            }
        ).also { it.start() }
    }

    fun stopSession() {
        stt?.stop()
        stt = null
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