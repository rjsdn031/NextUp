package lab.p4c.nextup.feature.settings.ui.experiment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun GenderButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors

    val backgroundColor = when {
        selected -> c.primary
        else -> c.surfaceVariant
    }

    val contentColor = when {
        selected -> c.onPrimary
        else -> x.textSecondary
    }

    Surface(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.clickableThrottle(onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        tonalElevation = if (enabled) 0.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
            )
        }
    }
}