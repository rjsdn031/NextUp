package lab.p4c.nextup.feature.overlay.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OverlayActionButton(
    isEligible: Boolean,
    isListening: Boolean,
    onToggleListening: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Box(modifier = modifier) {
        if (!isEligible) {
            FloatingActionButton(
                onClick = onToggleListening,
                containerColor = c.primary,
                contentColor = c.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.size(72.dp)
            ) {
                if (isListening) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                } else {
                    Icon(Icons.Filled.Mic, contentDescription = null)
                }
            }
        } else {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = c.onPrimary
                )
            ) {
                Text("이용하기", style = t.titleMedium)
            }
        }
    }
}