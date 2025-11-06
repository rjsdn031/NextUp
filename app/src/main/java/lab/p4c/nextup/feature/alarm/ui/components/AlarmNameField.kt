package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun AlarmNameField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("알람 이름", color = c.onSurfaceVariant, style = t.labelLarge) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedTextColor = c.onSurface,
            unfocusedTextColor = c.onSurface,
            cursorColor = c.onSurfaceVariant,
            focusedContainerColor = c.surface,
            unfocusedContainerColor = c.surface,
            focusedIndicatorColor = c.primary,
            unfocusedIndicatorColor = c.outline,
            focusedLabelColor = c.primary,
            unfocusedLabelColor = c.onSurfaceVariant,
            disabledTextColor = c.onSurface.copy(alpha = 0.38f)
        )
    )
}
