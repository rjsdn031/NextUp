package lab.p4c.nextup.feature.overlay.infra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase
import lab.p4c.nextup.feature.overlay.ui.util.getSimilarity
import kotlin.math.max
import kotlin.math.min

class SpeechUnlockSession(
    private val context: Context,
    private val targetPhrase: String,
    private val onPhase: (UnlockPhase) -> Unit,
    private val onPartial: (String, Float) -> Unit,
    private val onSuccess: () -> Unit,
    private val onErrorUi: (Int) -> Unit
) {
    private val main = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isDestroyed = false
    private var locked = false

    fun start() {
        if (isDestroyed || isListening || locked) return
        ensureRecognizer()
        if (recognizer == null) {
            onPhase(UnlockPhase.ClientErr)
            return
        }

        isListening = true
        onPhase(UnlockPhase.Listening)

        main.postDelayed({
            recognizer?.startListening(koreanIntent())
        }, 150)
    }

    fun stop() {
        isDestroyed = true
        isListening = false
        stopAndDestroy()
        if (!locked) onPhase(UnlockPhase.Idle)
    }

    private fun ensureRecognizer() {
        if (recognizer == null) {
            try {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(listener)
                }
            } catch (e: Exception) {
                Log.e("SpeechUnlock", "SpeechRecognizer init fail: ${e.message}")
                recognizer = null
            }
        }
    }

    private fun stopAndDestroy() {
        main.post {
            try { recognizer?.cancel() } catch (_: Throwable) {}
            try { recognizer?.stopListening() } catch (_: Throwable) {}
            try { recognizer?.destroy() } catch (_: Throwable) {}
            recognizer = null
        }
    }

    private fun koreanIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    private val listener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            onPhase(UnlockPhase.Listening)
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            onPhase(UnlockPhase.Processing)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            if (locked) return

            val hyp = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            Log.d("SpeechUnlock", "partial hyp=$hyp")

            if (hyp.isNotBlank()) {

                val (_, sim) = getSimilarity(targetPhrase, hyp)
                Log.d("SpeechUnlock", "partial sim=$sim target=$targetPhrase")

                onPartial(hyp, sim)

                if (isSuccess(hyp, targetPhrase, sim)) {
                    locked = true
                    isListening = false
                    stopAndDestroy()

                    onPartial(targetPhrase, 1f)
                    onPhase(UnlockPhase.Matched)
                    onSuccess()
                }
            }
        }

        override fun onResults(results: Bundle?) {
            if (locked) return

            val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            Log.d("SpeechUnlock", "final best=$best")

            val (_, sim) = getSimilarity(targetPhrase, best)
            Log.d("SpeechUnlock", "final sim=$sim target=$targetPhrase")

            isListening = false

            if (isSuccess(best, targetPhrase, sim)) {
                locked = true
                stopAndDestroy()
                onPartial(targetPhrase, 1f)
                onPhase(UnlockPhase.Matched)
                onSuccess()
            } else {
                onPhase(UnlockPhase.Mismatch)
            }
        }

        override fun onError(error: Int) {
            Log.e("SpeechUnlock", "onError=$error")

            if (locked) return

            isListening = false
            locked = false

            onErrorUi(error)

            val phase = when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> UnlockPhase.PermissionErr
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> UnlockPhase.Busy
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> UnlockPhase.Timeout
                SpeechRecognizer.ERROR_NO_MATCH -> UnlockPhase.Mismatch
                else -> UnlockPhase.ClientErr
            }
            onPhase(phase)

            stopAndDestroy()
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun isSuccess(hyp: String, target: String, sim: Float): Boolean {
        return sim >= 0.80f
    }

}
