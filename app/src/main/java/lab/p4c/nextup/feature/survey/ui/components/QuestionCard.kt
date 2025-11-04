package lab.p4c.nextup.feature.survey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QuestionCard(
    question: String,
    options: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(question, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                OutlinedButton(
                    onClick = { onSelect(index) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected == index) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
fun QuestionCardText(
    question: String,
    text: String,
    onChange: (String) -> Unit
) {
    Column {
        Text(question, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onChange,
            placeholder = { Text("간단히 입력해주세요") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
    }
}
