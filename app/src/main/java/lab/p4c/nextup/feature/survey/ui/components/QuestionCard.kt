package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QuestionCard(
    question: String,
    desc: String,
    options: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Column(modifier = Modifier.alpha(if (enabled) 1f else 0.35f)) {
        Text(question, style = t.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(desc, style = t.bodyMedium, color = c.onSurface.copy(alpha = 0.75f))
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, label ->
                OutlinedButton(
                    onClick = { if (enabled) onSelect(index) },
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor =
                            if (selected == index && enabled)
                                c.primaryContainer
                            else Color.Transparent,
                        contentColor =
                            if (selected == index && enabled)
                                c.onPrimaryContainer
                            else c.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, style = t.titleSmall)
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
    placeholder: String = "간단히 입력해주세요",
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

    Column(modifier = Modifier.alpha(if (enabled) 1f else 0.35f)) {

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
                showSubmit && onSubmit  != null -> {
                    Button(
                        onClick = onSubmit,
                        enabled = enabledSubmit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("제출")
                    }
                }

                showNext && onNext != null -> {
                    Button(
                        onClick = onNext,
                        enabled = text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("다음")
                    }
                }
            }
        }
    }
}