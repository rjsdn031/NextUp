package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Add로 화면 이동
 */
@Composable
fun AlarmFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme

    FloatingActionButton(
        onClick = onClick,
        containerColor = c.primary,
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "add", tint = c.onPrimary)
    }
}