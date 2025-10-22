package lab.p4c.nextup.feature.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlockingOverlayView(
    onDismiss: () -> Unit,
    onStartListening: () -> Unit, // 🎙️ STT 시작 콜백
    onStopListening: () -> Unit,  // ⏹ STT 중지 콜백
    onConfirm: () -> Unit,
    onBind: (
        setTarget: (String) -> Unit,
        setState: (String) -> Unit,
        setPartial: (hyp: String, sim: Float) -> Unit
    ) -> Unit
) {
    var title by remember { mutableStateOf("YOUTUBE를 계속 이용하려면 아래 문장을 또박또박 따라 말하세요") }
    var target by remember { mutableStateOf("문장을 불러오는 중…") }
    var stateText by remember { mutableStateOf("준비 중…") }
    var partial by remember { mutableStateOf("") }
    var similarity by remember { mutableFloatStateOf(0f) }

    var isListening by remember { mutableStateOf(false) }

    val threshold = 0.87f
    val eligible = similarity >= threshold && partial.isNotBlank()

    LaunchedEffect(Unit) {
        onBind(
            { t -> target = t },
            { s -> stateText = s },
            { h, s -> partial = h; similarity = s.coerceIn(0f, 1f) }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .pointerInput(Unit) {
//                detectTapGestures(onDoubleTap = { onDismiss() })
            },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Block,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = target,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { similarity },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "유사도 ${(similarity * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFBDBDBD)
            )

            Spacer(Modifier.height(16.dp))
            Text(text = stateText, color = Color(0xFFEEEEEE))

            if (partial.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "인식: $partial",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBDBDBD),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(24.dp))

            // 🎙️ 말하기 버튼
            Button(
                onClick = {
                    if (!isListening) {
                        onStartListening()
                    } else {
                        onStopListening()
                    }
                    isListening = !isListening
                }
            ) {
                Text(if (!isListening) "🎙️ 말하기 시작" else "⏹ 중지")
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { /* start/stop 토글 */ }) { /* ... */ }

        Button(
            enabled = eligible,
            onClick = onConfirm
        ) { Text("이용하기") }
    }
}