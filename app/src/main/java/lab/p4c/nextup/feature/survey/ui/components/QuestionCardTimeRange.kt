package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.util.clickableThrottle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCardTimeRange(
    question: String,
    startTime: String,
    endTime: String,
    enabled: Boolean,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    showNext: Boolean,
    onNext: () -> Unit
) {
    val t = MaterialTheme.typography

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startInit = parseHHmmOrNull(startTime) ?: (23 to 0)
    val endInit = parseHHmmOrNull(endTime) ?: (7 to 0)

    Column(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.35f)
            .padding(horizontal = 24.dp)
    ) {
        // 질문 여기 있음
        Text(question, style = t.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReadOnlyTimeField(
                label = "취침",
                value = startTime,
                enabled = enabled,
                onClick = { showStartPicker = true },
                modifier = Modifier.weight(1f)
            )

            ReadOnlyTimeField(
                label = "기상",
                value = endTime,
                enabled = enabled,
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f)
            )
        }

        // NEXT
        if (enabled && showNext) {
            Spacer(Modifier.height(12.dp))
            ThrottleButton(
                onClick = onNext,
                enabled = startTime.isNotBlank() && endTime.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음")
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showStartPicker) {
        TimePickerDialogCompat(
            title = "취침 시간",
            initialHour = startInit.first,
            initialMinute = startInit.second,
            onDismiss = { showStartPicker = false },
            onConfirm = { h, m ->
                onStartChange(formatHHmm(h, m))
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        TimePickerDialogCompat(
            title = "기상 시간",
            initialHour = endInit.first,
            initialMinute = endInit.second,
            onDismiss = { showEndPicker = false },
            onConfirm = { h, m ->
                onEndChange(formatHHmm(h, m))
                showEndPicker = false
            }
        )
    }
}

@Composable
private fun ReadOnlyTimeField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            enabled = enabled,
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("HH:mm") },
            leadingIcon = { Icon(imageVector = Icons.Default.AccessTime, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickableThrottle(enabled) { onClick() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogCompat(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            ThrottleButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("확인")
            }
        },
        dismissButton = {
            ThrottleOutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatHHmm(h: Int, m: Int): String =
    String.format(Locale.US, "%02d:%02d", h, m)

private fun parseHHmmOrNull(s: String): Pair<Int, Int>? {
    val p = s.trim().split(":")
    if (p.size != 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h to m
}
