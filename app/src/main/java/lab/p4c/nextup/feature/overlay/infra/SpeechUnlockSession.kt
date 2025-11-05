package lab.p4c.nextup.feature.overlay.infra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase
import kotlin.math.max
import kotlin.math.min

class SpeechUnlockSession(
    private val context: Context,
    private val targetPhrase: String,
    private val onPhase: (UnlockPhase) -> Unit,         // 상태 표시 ("듣는 중…" 등)
    private val onPartial: (String, Float) -> Unit, // 부분 인식 + 유사도
    private val onSuccess: () -> Unit,
    private val onErrorUi: (Int) -> Unit            // 에러 코드 전달(로그/UI)
) {
    private val main = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isDestroyed = false

    fun start() {
        if (isDestroyed || isListening) return
        ensureRecognizer()
        isListening = true
        onPhase(UnlockPhase.Listening)
        recognizer?.startListening(koreanOfflineIntent())
    }

    fun stop() {
        isDestroyed = true
        isListening = false
        safeStopAndDestroy()
        onPhase(UnlockPhase.Idle)
    }

    private fun ensureRecognizer() {
        if (recognizer == null) {
            // 일부 기기에서 앱 컨텍스트로 ERROR_CLIENT 빈발 → 가능하면 서비스/액티비티 컨텍스트 권장
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
        }
    }

    private fun safeStopAndDestroy() {
        main.post {
            try { recognizer?.cancel() } catch (_: Throwable) {}
            try { recognizer?.stopListening() } catch (_: Throwable) {}
            try { recognizer?.destroy() } catch (_: Throwable) {}
            recognizer = null
        }
    }

    private fun koreanOfflineIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { onPhase(UnlockPhase.Listening) }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { onPhase(UnlockPhase.Processing) }

        override fun onPartialResults(partialResults: Bundle?) {
            val hyp = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (hyp.isNotEmpty()) {
                val sim = similarity(hyp, targetPhrase)
                onPartial(hyp, sim)
                if (isSuccess(hyp, targetPhrase, sim)) {
                    isListening = false
                    onSuccess()
                    onPhase(UnlockPhase.Matched)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            val sim = similarity(best, targetPhrase)
            if (isSuccess(best, targetPhrase, sim)) {
                isListening = false
                onSuccess()
                onPhase(UnlockPhase.Matched)
            } else {
                // 실패해도 자동 재시작하지 않음 (버튼으로 다시 시도)
                isListening = false
                onPhase(UnlockPhase.Mismatch)
            }
        }

        override fun onError(error: Int) {
            isListening = false
            onErrorUi(error)
            val phase = when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> UnlockPhase.PermissionErr
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> UnlockPhase.Busy
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> UnlockPhase.Timeout
                SpeechRecognizer.ERROR_NO_MATCH -> UnlockPhase.Mismatch
                else -> UnlockPhase.ClientErr
            }
            onPhase(phase)
            // Todo: ClientErr일때 확인용 코드 추가
            safeStopAndDestroy()
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // ===== 매칭 규칙: 유사도 + 숫자 완전일치 =====
    private fun normalize(s: String) = s
        .lowercase()
        .replace(Regex("\\p{Punct}"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun similarity(aRaw: String, bRaw: String): Float {
        val a = normalize(aRaw); val b = normalize(bRaw)
        if (a.isEmpty() || b.isEmpty()) return 0f
        val dist = levenshtein(a, b)
        val maxLen = max(a.length, b.length).coerceAtLeast(1)
        return 1f - dist.toFloat() / maxLen.toFloat()
    }

    private fun isSuccess(hyp: String, target: String, sim: Float): Boolean {
        val tDigits = Regex("\\d+").findAll(target).map { it.value }.toList()
        val hDigits = Regex("\\d+").findAll(hyp).map { it.value }.toList()
        val digitsOk = tDigits.isEmpty() || tDigits == hDigits
        return digitsOk && sim >= 0.87f
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = i - 1
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                dp[j] = min(
                    min(dp[j] + 1, dp[j - 1] + 1),
                    prev + if (a[i - 1] == b[j - 1]) 0 else 1
                )
                prev = temp
            }
        }
        return dp[b.length]
    }
}
