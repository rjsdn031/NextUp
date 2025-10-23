package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalConfiguration
import lab.p4c.nextup.core.common.time.formatDateTime
import java.time.ZonedDateTime

@Composable
fun AlarmHeader(
    now: ZonedDateTime,
    nextAlarmMessage: String,
    modifier: Modifier = Modifier
) {
    val conf = LocalConfiguration.current
    val headerHeight: Dp = (conf.screenHeightDp * 0.3f).dp
    val zdt = now

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = nextAlarmMessage,
                color = Color.White,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize, // ~24sp
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatDateTime(zdt),
                color = Color(0xFF9E9E9E), // 회색
                fontSize = MaterialTheme.typography.titleSmall.fontSize, // ~16sp
                fontWeight = FontWeight.Medium
            )
        }
    }
}
