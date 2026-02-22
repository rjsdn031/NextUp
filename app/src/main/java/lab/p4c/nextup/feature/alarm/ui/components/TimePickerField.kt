package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.util.Locale
import lab.p4c.nextup.app.ui.util.clickableThrottle

@Composable
fun AlarmTimePicker(
    hour: Int,                    // 0..23 (외부가 진실)
    minute: Int,                  // 0..59
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    use24h: Boolean = false,
    visibleCount: Int = 3
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val itemHeight = 80.dp
    val haptic = LocalHapticFeedback.current

    val safeHour = hour.coerceIn(0, 23)
    val safeMinute = minute.coerceIn(0, 59)

    // 외부 값 → 인덱스
    val amIndex = if (use24h) null else if (safeHour < 12) 0 else 1

    val hourIndex = if (use24h) {
        safeHour // 0..23
    } else {
        val h12 = (safeHour % 12).let { if (it == 0) 12 else it } // 1..12
        h12 - 1 // 0..11
    }

    val minuteIndex = safeMinute // 0..59

    fun emitHourChangeFromIndex(newHourIndex: Int, dir: Int) {
        if (use24h) {
            onTimeChange(newHourIndex.coerceIn(0, 23), minuteIndex)
            return
        }

        val prev12 = hourIndex + 1               // 1..12
        val next12 = newHourIndex.coerceIn(0, 11) + 1 // 1..12

        var nextAm = amIndex ?: 0 // 0=AM,1=PM

        // 11 -> 12 (앞으로) 또는 12 -> 11 (뒤로)에서만 토글
        val toggle =
            (dir > 0 && prev12 == 11 && next12 == 12) ||
                    (dir < 0 && prev12 == 12 && next12 == 11)

        if (toggle) nextAm = 1 - nextAm

        val h24 = to24Hour(hour12 = next12, amIndex = nextAm)
        onTimeChange(h24, minuteIndex)
    }

    fun emitMinuteChangeWithCarry(newMinuteIndex: Int, dir: Int) {
        val newMinute = newMinuteIndex.coerceIn(0, 59)

        // carry 조건:
        // +1에서 59 -> 0
        // -1에서 0 -> 59 (역방향도 자연스럽게 처리)
        val carry = when {
            dir > 0 && minuteIndex == 59 && newMinute == 0 -> +1
            dir < 0 && minuteIndex == 0 && newMinute == 59 -> -1
            else -> 0
        }

        if (carry == 0) {
            onTimeChange(safeHour, newMinute)
            return
        }

        val (newHour, finalMinute) = addMinutes(
            hour24 = safeHour,
            minute = minuteIndex,
            deltaMinutes = carry
        )
        onTimeChange(newHour, finalMinute) // finalMinute은 0 또는 59가 됨
    }

    fun emitAmPmChange(newAmIndex: Int) {
        val hour12 = (hourIndex + 1) // 1..12
        val h24 = to24Hour(hour12 = hour12, amIndex = newAmIndex)
        onTimeChange(h24, minuteIndex)
    }

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
            WheelPicker(
                size = if (use24h) 24 else 12,
                selectedIndex = hourIndex,
                itemHeight = itemHeight,
                visibleCount = visibleCount,
                weight = 2f,
                labelFor = { idx ->
                    if (use24h) String.format(Locale.KOREA, "%02d", idx)
                    else (idx + 1).toString()
                },
                onStep = { dir, newIndex ->
                    // iOS wheel: 굴러갈 때 step 단위로 들어옴
                    emitHourChangeFromIndex(newIndex, dir)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onTapSelected = {
                    val next = if (use24h) (hourIndex + 1) % 24 else (hourIndex + 1) % 12
                    emitHourChangeFromIndex(next, +1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
            )

            Text(
                ":",
                style = t.displayMedium,
                color = c.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            WheelPicker(
                size = 60,
                selectedIndex = minuteIndex,
                itemHeight = itemHeight,
                visibleCount = visibleCount,
                weight = 2f,
                labelFor = { idx -> String.format(Locale.KOREA, "%02d", idx) },
                onStep = { dir, newIndex ->
                    emitMinuteChangeWithCarry(newIndex, dir)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onTapSelected = {
                    val next = (minuteIndex + 1) % 60
                    emitMinuteChangeWithCarry(next, +1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
            )

            if (!use24h) {
                Spacer(Modifier.width(8.dp))

                WheelPicker(
                    size = 2,
                    selectedIndex = amIndex ?: 0,
                    itemHeight = itemHeight,
                    visibleCount = visibleCount,
                    weight = 1.3f,
                    smallFont = true,
                    labelFor = { if (it == 0) "AM" else "PM" },
                    onStep = { dir, newIndex ->
                        emitAmPmChange(newIndex)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onTapSelected = {
                        val next = ((amIndex ?: 0) + 1) % 2
                        emitAmPmChange(next)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                )
            }
        }
    }
}

private fun to24Hour(hour12: Int, amIndex: Int): Int {
    // hour12: 1..12, amIndex: 0(AM) / 1(PM)
    val base = hour12 % 12 // 12 -> 0, 1..11 -> 1..11
    return if (amIndex == 0) base else base + 12
}

private fun addMinutes(hour24: Int, minute: Int, deltaMinutes: Int): Pair<Int, Int> {
    // deltaMinutes: 보통 ±1
    val total = hour24 * 60 + minute + deltaMinutes
    val mod = ((total % (24 * 60)) + (24 * 60)) % (24 * 60)
    val h = mod / 60
    val m = mod % 60
    return h to m
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.WheelPicker(
    size: Int,
    selectedIndex: Int,
    itemHeight: Dp,
    visibleCount: Int,
    weight: Float,
    labelFor: (Int) -> String,
    onStep: (direction: Int, newIndex: Int) -> Unit,
    onTapSelected: () -> Unit,
    smallFont: Boolean = false,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val centerPadding = itemHeight * (visibleCount / 2)

    val base = remember(size) {
        val mid = Int.MAX_VALUE / 2
        mid - (mid % size)
    }
    val start = base + selectedIndex

    val state = rememberLazyListState(initialFirstVisibleItemIndex = start)
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    fun centeredAbsoluteIndex(listState: LazyListState): Int? {
        val layout = listState.layoutInfo
        val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
        val nearest = layout.visibleItemsInfo.minByOrNull { info ->
            kotlin.math.abs((info.offset + info.size / 2) - viewportCenter)
        } ?: return null
        return nearest.index
    }

    LaunchedEffect(size, selectedIndex) {
        var lastAbs: Int? = null
        snapshotFlow { centeredAbsoluteIndex(state) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { abs ->
                val prev = lastAbs
                lastAbs = abs
                if (prev == null) return@collect

                val diff = abs - prev
                val dir = when {
                    diff > 0 -> +1
                    diff < 0 -> -1
                    else -> 0
                }
                if (dir == 0) return@collect

                repeat(kotlin.math.abs(diff)) {
                    val stepAbs = prev + dir * (it + 1)
                    val stepIdx = floorMod(stepAbs, size)
                    onStep(dir, stepIdx)
                }
            }
    }

    LaunchedEffect(selectedIndex) {
        val abs = centeredAbsoluteIndex(state) ?: return@LaunchedEffect
        val current = floorMod(abs, size)
        if (current == selectedIndex) return@LaunchedEffect

        val targetAbs = abs + (selectedIndex - current)
        state.animateScrollToItem(targetAbs)
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
    ) {
        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = centerPadding),
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(count = Int.MAX_VALUE, key = { it }) { abs ->
                val idx = floorMod(abs, size)
                val isSelected = (idx == selectedIndex)

                Text(
                    text = labelFor(idx),
                    style = if (!smallFont) t.displayMedium else t.headlineMedium,
                    color = when {
                        isSelected -> c.onBackground
                        else -> c.onSurface.copy(alpha = 0.5f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .then(
                            if (isSelected) Modifier.clickableThrottle(onClick = onTapSelected) else Modifier
                        )
                )
            }
        }
    }
}

private fun floorMod(x: Int, m: Int): Int {
    val r = x % m
    return if (r < 0) r + m else r
}