package lab.p4c.nextup.feature.overlay.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import lab.p4c.nextup.feature.overlay.ui.UnlockPhase
import lab.p4c.nextup.feature.overlay.ui.util.phaseText

@Composable
fun OverlayPhaseText(phase: UnlockPhase) {
    Text(
        text = phaseText(phase),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}