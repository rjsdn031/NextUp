package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DismissSlider(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SlideToAct(
            text = "밀어서 해제",
            onComplete = onComplete
        )
    }
}

@Composable
fun SlideToAct(
    modifier: Modifier = Modifier,
    text: String = "밀어서 해제",
    onComplete: () -> Unit,
    height: Dp = 60.dp,
    knobSize: Dp = 44.dp,
    cornerRadius: Dp = 999.dp, // pill
    threshold: Float = 0.92f,  // 완료 임계치
    enabled: Boolean = true,
    trackColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary,
    knobColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    knobIconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) } // 0f..1f
    var fired by remember { mutableStateOf(false) }
    var thresholdBuzzed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
            .semantics {
                role = Role.Button
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val maxW = constraints.maxWidth.toFloat()
        val hPx = with(LocalDensity.current) { height.toPx() }
        val knobPx = with(LocalDensity.current) { knobSize.toPx() }
        val paddingPx = (hPx - knobPx) / 2f
        val travelPx = (maxW - knobPx - 2 * paddingPx).coerceAtLeast(0f)

        // 가운데 라벨
        Text(
            text = text,
            color = labelColor.copy(alpha = if (enabled) 1f else 0.5f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.align(Alignment.Center)
        )

        // 손잡이 위치
        val x = paddingPx + progress.value * travelPx

        // 손잡이
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .padding(horizontal = Dp(paddingPx / LocalDensity.current.density))
                .size(knobSize)
                .offset(x = Dp(x / LocalDensity.current.density))
                .clip(CircleShape)
                .background(knobColor)
                .draggable(
                    enabled = enabled && !fired,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val new = (progress.value + delta / travelPx).coerceIn(0f, 1f)
                        scope.launch { progress.snapTo(new) }   // ← 여기에 launch 추가!
                        if (!thresholdBuzzed && new >= threshold) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            thresholdBuzzed = true
                        }
                        if (thresholdBuzzed && new < threshold - 0.05f) {
                            thresholdBuzzed = false
                        }
                    },
                    onDragStopped = {
                        if (progress.value >= threshold && !fired) {
                            fired = true
                            scope.launch {
                                progress.animateTo(1f, animationSpec = tween(120))
                                onComplete()
                            }
                        } else {
                            scope.launch {
                                progress.animateTo(0f, animationSpec = tween(180))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = knobIconTint
            )
        }
    }
}