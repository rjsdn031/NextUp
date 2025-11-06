package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AlarmTimePicker(
    hour: Int,                    // 0..23 (외부가 진실)
    minute: Int,                  // 0..59
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    use24h: Boolean = false,      // 12시간 고정이면 false
    visibleCount: Int = 3
) {

    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val itemHeight = 80.dp
    val haptic = LocalHapticFeedback.current

    // 외부 값 → 인덱스
    val amIndex = if (use24h) null else if (hour < 12) 0 else 1
    val hourIndex = if (use24h) {
        hour.coerceIn(0, 23)              // 0..23
    } else {
        val h12 = (hour % 12).let { if (it == 0) 12 else it } // 1..12
        (h12 - 1)                          // 0..11
    }
    val minuteIndex = minute.coerceIn(0, 59)

    Surface(
        color = c.background,
        contentColor = c.onBackground,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(itemHeight * visibleCount),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            // 시 (원형 12칸)
            StepperWheel(
                size = if (use24h) 24 else 12,
                selectedIndex = hourIndex,
                onChange = { idx ->
                    if (use24h) {
                        onTimeChange(idx, minuteIndex)
                    } else {
                        val sel = (idx + 1) % 12
                        val newH = if (amIndex == 0) sel else sel + 12
                        onTimeChange(newH, minuteIndex)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                labelFor = { idx ->
                    if (use24h) String.format(Locale.KOREA, "%02d", idx)
                    else ((idx + 1).let { if (it == 12) 12 else it }).toString()
                },
                itemHeight = itemHeight,
                visibleCount = visibleCount,
                weight = 2f
            )

            Text(
                ":",
                style = t.displayMedium,
                color = c.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // 분 (원형 60칸)
            StepperWheel(
                size = 60,
                selectedIndex = minuteIndex,
                onChange = { idx ->
                    onTimeChange(hour, idx)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                labelFor = { idx -> String.format(Locale.KOREA, "%02d", idx) },
                itemHeight = itemHeight,
                visibleCount = visibleCount,
                weight = 2f,
            )

            if (!use24h) {
                Spacer(Modifier.width(8.dp))
                // AM/PM (원형 2칸)
                StepperWheel(
                    size = 2,
                    selectedIndex = amIndex ?: 0, // 0=AM,1=PM
                    onChange = { a ->
                        val sel = (hourIndex + 1) % 12
                        val newH = if (a == 0) sel else sel + 12
                        onTimeChange(newH, minuteIndex)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    labelFor = { if (it == 0) "AM" else "PM" },
                    itemHeight = itemHeight,
                    visibleCount = visibleCount,
                    weight = 1.3f,
                    smallFont = true
                )
            }
        }
    }
}

@Composable
private fun RowScope.StepperWheel(
    size: Int,                         // 항목 개수 (예: 12, 60, 2)
    selectedIndex: Int,                // 0..size-1 (외부가 진실)
    onChange: (Int) -> Unit,           // 인덱스 변경 콜백
    labelFor: (Int) -> String,         // 표시 문자열
    itemHeight: Dp,
    visibleCount: Int,
    weight: Float,
    smallFont: Boolean = false
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    // 드래그 누적
    var dragPx by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    // 보이는 라벨: 중앙(선택), 위/아래 이웃만 그려 단순화
    fun wrap(i: Int): Int {
        val m = i % size
        return if (m < 0) m + size else m
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
    ) {
        // 라벨 영역
        Column(
            modifier = Modifier
                .matchParentSize()
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        dragPx += delta
                        while (dragPx <= -itemHeightPx) {
                            dragPx += itemHeightPx
                            onChange(wrap(selectedIndex + 1))
                        }
                        while (dragPx >= itemHeightPx) {
                            dragPx -= itemHeightPx
                            onChange(wrap(selectedIndex - 1))
                        }
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 위쪽(이전)
            if (visibleCount >= 3) {
                Text(
                    text = labelFor(wrap(selectedIndex - 1)),
                    style = if (!smallFont) t.displayMedium else t.headlineMedium,
                    color = c.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(itemHeight)
                )
            }

            // 중앙(선택)
            Text(
                text = labelFor(selectedIndex),
                style = if (!smallFont) t.displayMedium else t.headlineMedium,
                color = c.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(itemHeight)
            )

            // 아래쪽(다음)
            if (visibleCount >= 3) {
                Text(
                    text = labelFor(wrap(selectedIndex + 1)),
                    style = if (!smallFont) t.displayMedium else t.headlineMedium,
                    color = c.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(itemHeight)
                )
            }
        }
    }
}
