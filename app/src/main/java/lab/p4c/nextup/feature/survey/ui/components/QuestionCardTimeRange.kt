package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.util.clickableThrottle
import java.util.Locale

/**
 * Time range survey component for self-reported sleep.
 *
 * Displays two read-only time fields ("Sleep" and "Wake")
 * and opens a Material3 [TimePicker] dialog when tapped.
 *
 * Characteristics:
 * - Stateless UI component (time values are provided externally).
 * - Uses read-only [OutlinedTextField] to prevent manual typing.
 * - Time selection is performed exclusively via dialog.
 * - Uses project-level throttled buttons for dialog actions.
 *
 * Time format:
 * - UI uses "HH:mm" 24-hour format.
 * - Validation is handled by the ViewModel layer.
 *
 * @param question The question title displayed above the time inputs.
 * @param startTime Sleep start time in "HH:mm" format.
 * @param endTime Wake time in "HH:mm" format.
 * @param enabled Whether interaction is allowed.
 * @param onStartChange Callback invoked when sleep time changes.
 * @param onEndChange Callback invoked when wake time changes.
 * @param modifier Optional modifier for layout customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCardTimeRange(
    question: String,
    startTime: String,
    endTime: String,
    enabled: Boolean,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = MaterialTheme.typography

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startInit = parseHHmmOrNull(startTime) ?: (23 to 0)
    val endInit = parseHHmmOrNull(endTime) ?: (7 to 0)

    Column(modifier = modifier) {
        Text(text = question, style = t.titleLarge)
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

/**
 * Read-only time input field that opens a dialog when tapped.
 *
 * The underlying [OutlinedTextField] is marked as read-only
 * and covered by a full-size clickable overlay to intercept taps.
 *
 * This prevents manual editing and ensures controlled time selection.
 *
 * @param label Field label.
 * @param value Current time value in "HH:mm" format.
 * @param enabled Whether interaction is allowed.
 * @param onClick Callback triggered when the field is tapped.
 * @param modifier Optional modifier.
 */
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

/**
 * Wrapper around Material3 [TimePicker] displayed inside an [AlertDialog].
 *
 * Uses project-level throttled buttons for confirm/dismiss actions.
 *
 * @param title Dialog title.
 * @param initialHour Initial hour for the picker.
 * @param initialMinute Initial minute for the picker.
 * @param onDismiss Callback when dialog is dismissed.
 * @param onConfirm Callback when time selection is confirmed.
 */
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

/**
 * Formats hour and minute into "HH:mm" 24-hour string.
 */
private fun formatHHmm(h: Int, m: Int): String =
    String.format(Locale.US, "%02d:%02d", h, m)

/**
 * Parses a "HH:mm" string into a (hour, minute) pair.
 *
 * Returns null if:
 * - The format is invalid
 * - The values are out of 24-hour range
 */
private fun parseHHmmOrNull(s: String): Pair<Int, Int>? {
    val p = s.trim().split(":")
    if (p.size != 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h to m
}