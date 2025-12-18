package lab.p4c.nextup.feature.overlay.ui

import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lab.p4c.nextup.app.ui.theme.NextUpTheme
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.feature.blocking.infra.BlockGate
import lab.p4c.nextup.feature.overlay.infra.BlockingOverlayController
import javax.inject.Inject

@AndroidEntryPoint
class OverlayHostActivity : ComponentActivity() {

    @Inject lateinit var blockGate: BlockGate
    @Inject lateinit var telemetryLogger: TelemetryLogger

    private val overlaySessionId: String = UUID.randomUUID().toString()
    private var target: String = ""

    private var hiddenLogged = false
    private var timeoutJob: Job? = null

    private var currentAttemptId: String? = null
    private var attemptFinalized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OverlayState.isRunning = true

        target = intent.getStringExtra("appLabel") ?: ""
        val phrase = intent.getStringExtra("phrase") ?: ""

        // OverlayShown
        telemetryLogger.log(
            eventName = "OverlayShown",
            payload = mapOf(
                "overlaySessionId" to overlaySessionId,
                "target" to target,
                "unlockSentence" to phrase
            )
        )

        // OverlayTimeOut
        timeoutJob = lifecycleScope.launch {
            delay(10 * 60_000L)

            val pm = getSystemService<PowerManager>()
            val screenOn = pm?.isInteractive == true

            telemetryLogger.log(
                eventName = "OverlayTimeOut",
                payload = mapOf(
                    "overlaySessionId" to overlaySessionId,
                    "ScreenState" to screenOn.toString()
                )
            )

            // 타임아웃 시 세션 종료 + 화면 닫기
            BlockingOverlayController.stopSession()
            finish()
        }

        setContent {
            NextUpTheme {
                BlockingOverlayView(
                    title = "${target}를 계속 이용하려면\n아래 문장을 따라 말하세요",
                    onDismiss = {
                        BlockingOverlayController.stopSession()
                        finish()
                    },
                    onStartListening = {
                        val attemptId = UUID.randomUUID().toString()
                        currentAttemptId = attemptId
                        attemptFinalized = false

                        telemetryLogger.log(
                            eventName = "SpeechUnlockStarted",
                            payload = mapOf(
                                "overlaySessionId" to overlaySessionId,
                                "attemptId" to attemptId
                            )
                        )

                        BlockingOverlayController.startSession(
                            activity = this,
                            targetPhrase = phrase,
                            onUnlocked = {
                                if (!attemptFinalized) {
                                    attemptFinalized = true
                                    telemetryLogger.log(
                                        eventName = "SpeechUnlockSucceeded",
                                        payload = mapOf(
                                            "overlaySessionId" to overlaySessionId,
                                            "attemptId" to attemptId
                                        )
                                    )
                                }
                            },
                            onFailed = { phase ->
                                if (!attemptFinalized) {
                                    attemptFinalized = true
                                    telemetryLogger.log(
                                        eventName = "SpeechUnlockFailed",
                                        payload = mapOf(
                                            "overlaySessionId" to overlaySessionId,
                                            "attemptId" to attemptId,
                                            "Reason" to phase.name
                                        )
                                    )
                                }
                            }
                        )
                    },
                    onStopListening = {
                        BlockingOverlayController.stopSession()

                        val attemptId = currentAttemptId
                        if (attemptId != null && !attemptFinalized) {
                            attemptFinalized = true
                            telemetryLogger.log(
                                eventName = "SpeechUnlockFailed",
                                payload = mapOf(
                                    "overlaySessionId" to overlaySessionId,
                                    "attemptId" to attemptId,
                                    "Reason" to UnlockPhase.Idle.name
                                )
                            )
                        }
                    },
                    onConfirm = {
                        telemetryLogger.log(
                            eventName = "AppUseClicked",
                            payload = mapOf(
                                "overlaySessionId" to overlaySessionId
                            )
                        )

                        BlockingOverlayController.stopSession()
                        val intent = Intent("lab.p4c.nextup.OVERLAY_UNLOCKED")
                        sendBroadcast(intent)
                        blockGate.disableUntilNextAlarm()
                        blockGate.clearReady()
                        Log.d("AppA11y", "Unlocked — blocking disabled until next alarm")

                        setResult(RESULT_OK)
                        finish()
                    },
                    onBind = { setTarget, setPhase, setPartial ->
                        BlockingOverlayController.bind(
                            setTarget, setPhase, setPartial, phrase
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        OverlayState.isRunning = false
        timeoutJob?.cancel()
        BlockingOverlayController.stopSession()

        // OverlayHidden (중복 방지)
        logHiddenOnce()
    }

    private fun logHiddenOnce() {
        if (hiddenLogged) return
        hiddenLogged = true

        telemetryLogger.log(
            eventName = "OverlayHidden",
            payload = mapOf(
                "overlaySessionId" to overlaySessionId,
                "target" to target
            )
        )
    }
}
