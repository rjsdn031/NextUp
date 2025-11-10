package lab.p4c.nextup.feature.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.feature.overlay.ui.util.getSimilarity

@Composable
fun BlockingOverlayView(
    onDismiss: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onConfirm: () -> Unit,
    onBind: (
        setTarget: (String) -> Unit,
        setPhase: (UnlockPhase) -> Unit,
        setPartial: (hyp: String, sim: Float) -> Unit
    ) -> Unit,
    threshold: Float = 0.90f
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography
    val x = NextUpThemeTokens.colors

    var title by remember { mutableStateOf("YOUTUBE를 계속 이용하려면\n아래 문장을 또박또박 따라 말하세요") }
    var target by remember { mutableStateOf("문장을 불러오는 중…") }
    var phase by remember { mutableStateOf(UnlockPhase.Idle) }
    var partial by remember { mutableStateOf("") }
    var similarity by remember { mutableFloatStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }

    val eligible = similarity >= threshold && partial.isNotBlank()

    LaunchedEffect(Unit) {
        onBind(
            { t -> target = t },
            { p -> phase = p },
            { h, s -> partial = h; similarity = s.coerceIn(0f, 1f) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.scrim.copy(alpha = 0.75f))
            .pointerInput(Unit) { /* 터치 차단 */ }
            .padding(horizontal = 24.dp)
    ) {
        // 본문
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Block,
                contentDescription = null,
                tint = c.error.copy(alpha = 0.9f),
                modifier = Modifier.size(150.dp)
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = t.titleMedium,
                color = c.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            val annotated = remember(target, partial) {
                val (res, _) = getSimilarity(target, partial)
                buildAnnotatedStringFromMatch(
                    target = target,
                    matched = res.matchedInTarget,
                    matchedStyle = SpanStyle(
                        color = c.onBackground,
                        fontWeight = FontWeight.SemiBold
                    ),
                    dimmedStyle = SpanStyle(color = x.textMuted)
                )
            }

            Text(
                text = annotated,
                style = t.titleLarge,
                color = c.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = phaseText(phase),
                style = t.bodyLarge,
                color = c.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (!eligible) {
            FloatingActionButton(
                onClick = {
                    if (!isListening) onStartListening() else onStopListening()
                    isListening = !isListening
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .offset(y = (-88).dp)
                    .size(72.dp),
                containerColor = c.primary,
                contentColor = c.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                if (isListening) {
                    Icon(Icons.Filled.Stop, contentDescription = "중지")
                } else {
                    Icon(Icons.Filled.Mic, contentDescription = "말하기 시작")
                }
            }
        } else {
            isListening = false
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .offset(y = (-88).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = c.onPrimary
                )
            ) {
                Text("이용하기", style = t.titleMedium)
            }
        }
    }
}

private fun buildAnnotatedStringFromMatch(
    target: String,
    matched: BooleanArray,
    matchedStyle: SpanStyle,
    dimmedStyle: SpanStyle
): AnnotatedString {
    if (target.isEmpty() || matched.isEmpty()) return AnnotatedString(target)

    return buildAnnotatedString {
        var runStart = 0
        var runMatched = matched.getOrElse(0) { false }
        for (idx in 1 until target.length) {
            val sameBucket = matched.getOrElse(idx) { false } == runMatched
            if (!sameBucket) {
                pushStyle(if (runMatched) matchedStyle else dimmedStyle)
                append(target.substring(runStart, idx))
                pop()
                runStart = idx
                runMatched = matched.getOrElse(idx) { false }
            }
        }
        pushStyle(if (runMatched) matchedStyle else dimmedStyle)
        append(target.substring(runStart, target.length))
        pop()
    }
}
