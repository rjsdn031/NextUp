package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun QuestionCard(
    question: String,
    desc: String = "",
    options: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Column(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.35f)
            .padding(horizontal = 24.dp)
    ) {

        Text(question, style = t.titleLarge)
//        Spacer(Modifier.height(6.dp))
//        Text(desc, style = t.bodySmall, color = c.onSurface.copy(alpha = 0.75f))
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { index, label ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .then(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ),
                    horizontalArrangement = Arrangement.Start
                ) {

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(20.dp)
                            .clickableThrottle(enabled) { onSelect(index) }
                    ) {
                        drawCircle(
                            color = c.onSurface.copy(alpha = if (enabled) 0.7f else 0.45f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun QuestionCardText(
    question: String,
    text: String,
    placeholder: String = "10자 이상 입력해주세요",
    enabled: Boolean,
    onChange: (String) -> Unit,
    showNext: Boolean = false,
    showSubmit: Boolean = false,
    onNext: (() -> Unit)? = null,
    enabledSubmit: Boolean = false,
    onSubmit: (() -> Unit)? = null
) {
    val t = MaterialTheme.typography
    val c = MaterialTheme.colorScheme

    Column(modifier = Modifier
        .alpha(if (enabled) 1f else 0.35f)
        .padding(horizontal = 24.dp)) {

        Text(question, style = t.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (enabled) onChange(it) },
            placeholder = { Text(placeholder) },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )

        // NEXT or SUBMIT
        if (enabled) {
            Spacer(Modifier.height(12.dp))

            when {
                showSubmit && onSubmit != null -> {
                    ThrottleButton(
                        onClick = onSubmit,
                        enabled = enabledSubmit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("제출")
                    }
                }

                showNext && onNext != null -> {
                    ThrottleButton(
                        onClick = onNext,
                        enabled = (text.isNotBlank() && text.trim().length >= 10),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("다음")
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCardTimeRange(
    question: String,
    startTime: String,
    endTime: String,
    enabled: Boolean,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    showNext: Boolean = false,
    onNext: (() -> Unit)? = null
) {
    val t = MaterialTheme.typography

    Column(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.35f)
            .padding(horizontal = 24.dp)
    ) {
        Text(question, style = t.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { if (enabled) onStartChange(it) },
                enabled = enabled,
                singleLine = true,
                placeholder = { Text("시작 (HH:mm)") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = endTime,
                onValueChange = { if (enabled) onEndChange(it) },
                enabled = enabled,
                singleLine = true,
                placeholder = { Text("끝 (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
        }

        if (enabled && showNext && onNext != null) {
            Spacer(Modifier.height(12.dp))
            ThrottleButton(
                onClick = onNext,
                enabled = startTime.isNotBlank() && endTime.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음")
            }
        }
    }
}