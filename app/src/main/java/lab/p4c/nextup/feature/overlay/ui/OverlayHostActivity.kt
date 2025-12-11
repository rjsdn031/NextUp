package lab.p4c.nextup.feature.overlay.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import lab.p4c.nextup.app.ui.theme.NextUpTheme
import lab.p4c.nextup.feature.blocking.infra.BlockGate
import lab.p4c.nextup.feature.overlay.infra.BlockingOverlayController
import javax.inject.Inject

@AndroidEntryPoint
class OverlayHostActivity : ComponentActivity() {

    @Inject
    lateinit var blockGate: BlockGate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OverlayState.isRunning = true

        val appLabel = intent.getStringExtra("appLabel") ?: ""
        val phrase = intent.getStringExtra("phrase") ?: ""

        setContent {
            NextUpTheme {
                BlockingOverlayView(
                    title = "${appLabel}를 계속 이용하려면\n아래 문장을 따라 말하세요",
                    onDismiss = {
                        BlockingOverlayController.stopSession()
                        finish()
                    },
                    onStartListening = {
                        BlockingOverlayController.startSession(
                            activity = this,
                            targetPhrase = phrase,
                            onUnlocked = { }
                        )
                    },
                    onStopListening = {
                        BlockingOverlayController.stopSession()
                    },
                    onConfirm = {
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
        BlockingOverlayController.stopSession()
    }
}
