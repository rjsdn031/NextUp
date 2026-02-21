package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun QuestionCard(
    question: String,
    options: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.35f)
    ) {
        Text(text = question, style = t.titleLarge)
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Canvas(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(20.dp)
                            .clickableThrottle(enabled) { onSelect(index) }
                    ) {
                        drawCircle(
                            color = c.onSurface.copy(alpha = if (enabled) 0.7f else 0.45f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        if (selected == index) {
                            drawCircle(
                                color = c.primary,
                                radius = size.minDimension / 3
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = label,
                        style = t.bodyLarge,
                        color = c.onSurface,
                        modifier = Modifier.clickableThrottle(enabled) { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCardText(
    question: String,
    text: String,
    placeholder: String = "10자 이상 입력해주세요",
    enabled: Boolean,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = MaterialTheme.typography
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.35f)) {
        Text(text = question, style = t.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (enabled) onChange(it) },
            placeholder = { Text(placeholder) },
            enabled = enabled,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = false,
            minLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
    }
}