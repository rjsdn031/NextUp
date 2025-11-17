package lab.p4c.nextup.feature.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.feature.overlay.ui.components.OverlayActionButton
import lab.p4c.nextup.feature.overlay.ui.components.OverlayHeader
import lab.p4c.nextup.feature.overlay.ui.components.OverlayMatchedText
import lab.p4c.nextup.feature.overlay.ui.components.OverlayPhaseText

@Composable
fun BlockingOverlayView(
    title: String,
    onDismiss: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onConfirm: () -> Unit,
    onBind: (
        setTarget: (String) -> Unit,
        setPhase: (UnlockPhase) -> Unit,
        setPartial: (String, Float) -> Unit
    ) -> Unit,
    threshold: Float = 0.90f
) {
    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors
    val t = MaterialTheme.typography

//    var title by remember { mutableStateOf("YOUTUBE를 계속 이용하려면\n아래 문장을 또박또박 따라 말하세요") }
    var target by remember { mutableStateOf("문장을 불러오는 중…") }
    var phase by remember { mutableStateOf(UnlockPhase.Idle) }
    var partial by remember { mutableStateOf("") }
    var similarity by remember { mutableFloatStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }

    val eligible = similarity >= threshold && partial.isNotBlank()

    LaunchedEffect(Unit) {
        onBind(
            { t -> target = t },
            { p -> phase = p },
            { hyp, sim ->
                partial = hyp
                similarity = sim.coerceIn(0f, 1f)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.scrim.copy(alpha = 0.75f))
            .pointerInput(Unit) { /* 터치 차단 */ }
            .padding(horizontal = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OverlayHeader()
            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = t.titleMedium,
                color = c.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            OverlayMatchedText(
                target = target,
                partial = partial,
                textColor = x.textMuted,
                matchedColor = c.onBackground
            )

            Spacer(Modifier.height(16.dp))

            OverlayPhaseText(phase = phase)
        }

        OverlayActionButton(
            isEligible = eligible,
            isListening = isListening,
            onToggleListening = {
                if (isListening) onStopListening() else onStartListening()
                isListening = !isListening
            },
            onConfirm = onConfirm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .offset(y = (-88).dp)
        )
    }
}