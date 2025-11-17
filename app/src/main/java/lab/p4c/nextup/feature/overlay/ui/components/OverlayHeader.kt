package lab.p4c.nextup.feature.overlay.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OverlayHeader() {
    val c = MaterialTheme.colorScheme
    Icon(
        Icons.Filled.Block,
        contentDescription = null,
        tint = c.error.copy(alpha = 0.9f),
        modifier = Modifier.size(150.dp)
    )
}
