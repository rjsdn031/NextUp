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
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors


    Surface(
        modifier = modifier
            .clickableThrottle(onClick)
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = if (selected) c.primary else c.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) c.onPrimary else x.textSecondary
            )
        }
    }
}