package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun AlarmAddTile(
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickableThrottle { onClick() },
        color = c.surface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "add", tint = c.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "알람 추가",
                    style = t.bodyLarge,
                    color = c.onSurfaceVariant
                )
            }
        }
    }
}