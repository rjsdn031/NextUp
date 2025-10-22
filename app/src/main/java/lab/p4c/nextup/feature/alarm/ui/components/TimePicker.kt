package lab.p4c.nextup.feature.alarm.ui.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    onTimePicked: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    useSystemPicker: Boolean = true,
) {
    val ctx = LocalContext.current
    // locale 지정 - Korea: 추후에 사용자 지정 시간으로 바꾸기
    val timeText = String.format(locale = Locale.KOREA,"%02d:%02d", hour, minute)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .clickable {
                if (useSystemPicker) {
                    showTimePicker(ctx, hour, minute, onTimePicked)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun showTimePicker(
    context: Context,
    hour: Int,
    minute: Int,
    onPicked: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, h, m -> onPicked(h, m) },
        hour, minute, true
    ).show()
}
