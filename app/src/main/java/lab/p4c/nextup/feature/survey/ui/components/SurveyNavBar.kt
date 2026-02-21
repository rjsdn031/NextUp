package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton

@Composable
fun SurveyNavBar(
    showPrev: Boolean,
    onPrev: () -> Unit,
    primaryText: String,      // "다음" or "제출"
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showPrev) {
                ThrottleOutlinedButton(
                    onClick = onPrev,
                    modifier = Modifier.weight(1f)
                ) { Text("이전") }
            }

            ThrottleButton(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(primaryText)
            }
        }
    }
}