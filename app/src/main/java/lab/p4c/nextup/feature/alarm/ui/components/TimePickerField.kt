package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import java.util.Locale
import lab.p4c.nextup.app.ui.util.clickableThrottle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

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
    val haptic = LocalHapticFeedback.current

    val itemHeight = 80.dp
    val safeHour = hour.coerceIn(0, 23)
    val safeMinute = minute.coerceIn(0, 59)

    /**
     * Wheel 스크롤 중 “외부 state”가 setState로 되돌려버리는 문제 방지용 로컬 truth.
     * - 외부 hour/minute가 바뀌면 동기화
     * - UI 조작(스크롤/탭) 시 즉시 반영
     */
    var curHour by remember { mutableIntStateOf(safeHour) }
    var curMinute by remember { mutableIntStateOf(safeMinute) }

    LaunchedEffect(safeHour, safeMinute) {
        curHour = safeHour
        curMinute = safeMinute
    }

    val amIndex = if (use24h) null else if (curHour < 12) 0 else 1

    val hourIndex = if (use24h) {
        curHour // 0..23
    } else {
        val h12 = curHour.to12Hour() // 1..12
        h12 - 1 // 0..11
    }

    val minuteIndex = curMinute // 0..59

    fun notify() = onTimeChange(curHour, curMinute)

    fun applyHourIndex(newHourIndex: Int, dir: Int) {
        if (use24h) {
            curHour = newHourIndex.coerceIn(0, 23)
            notify()
            return
        }

        val prev12 = curHour.to12Hour()                  // 1..12
        val next12 = newHourIndex.coerceIn(0, 11) + 1    // 1..12

        var nextAm = if (curHour < 12) 0 else 1          // 0=AM,1=PM
        val shouldToggle =
            (dir > 0 && prev12 == 11 && next12 == 12) ||
                    (dir < 0 && prev12 == 12 && next12 == 11)

        if (shouldToggle) nextAm = 1 - nextAm

        curHour = to24Hour(hour12 = next12, amIndex = nextAm)
        notify()
    }

    fun applyMinuteIndexWithCarry(newMinuteIndex: Int, dir: Int) {
        val newMinute = newMinuteIndex.coerceIn(0, 59)

        val carryHour = when {
            dir > 0 && curMinute == 59 && newMinute == 0 -> +1
            dir < 0 && curMinute == 0 && newMinute == 59 -> -1
            else -> 0
        }

        if (carryHour == 0) {
            curMinute = newMinute
            notify()
            return
        }

        val (newHour, finalMinute) = addMinutes(
            hour24 = curHour,
            minute = curMinute,
            deltaMinutes = carryHour
        )
        curHour = newHour
        curMinute = finalMinute
        notify()
    }

    fun applyAmPm(newAmIndex: Int) {
        val hour12 = curHour.to12Hour()
        curHour = to24Hour(hour12 = hour12, amIndex = newAmIndex)
        notify()
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
                onStep = { dir, idx ->
                    applyHourIndex(idx, dir)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onTapSelected = {
                    val next = if (use24h) (hourIndex + 1) % 24 else (hourIndex + 1) % 12
                    applyHourIndex(next, +1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )

            Text(
                text = ":",
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
                onStep = { dir, idx ->
                    applyMinuteIndexWithCarry(idx, dir)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onTapSelected = {
                    val next = (minuteIndex + 1) % 60
                    applyMinuteIndexWithCarry(next, +1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
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
                    onStep = { _, idx ->
                        applyAmPm(idx)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onTapSelected = {
                        val next = ((amIndex ?: 0) + 1) % 2
                        applyAmPm(next)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            }
        }
    }
}

private fun Int.to12Hour(): Int {
    val h = this % 12
    return if (h == 0) 12 else h
}

private fun to24Hour(hour12: Int, amIndex: Int): Int {
    // hour12: 1..12, amIndex: 0(AM) / 1(PM)
    val base = hour12 % 12 // 12 -> 0, 1..11 -> 1..11
    return if (amIndex == 0) base else base + 12
}

private fun addMinutes(hour24: Int, minute: Int, deltaMinutes: Int): Pair<Int, Int> {
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
    val startAbsIndex = base + selectedIndex

    val state = rememberLazyListState(initialFirstVisibleItemIndex = startAbsIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    val flingDamping = 0.45f // TODO: 튜닝
    val dampingConnection = remember(flingDamping) {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity(
                    x = available.x * flingDamping,
                    y = available.y * flingDamping
                )
            }
        }
    }

    fun floorMod(x: Int, m: Int): Int {
        val r = x % m
        return if (r < 0) r + m else r
    }

    fun findCenteredAbsIndex(listState: LazyListState): Int? {
        val layout = listState.layoutInfo
        if (layout.visibleItemsInfo.isEmpty()) return null

        val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
        val nearest = layout.visibleItemsInfo.minByOrNull { info ->
            kotlin.math.abs((info.offset + info.size / 2) - viewportCenter)
        } ?: return null
        return nearest.index
    }

    var previewIndex by remember { mutableIntStateOf(selectedIndex) }
    var committedAbs by remember { mutableIntStateOf(startAbsIndex) }

    // 외부 selectedIndex 동기화 중 emit 방지
    var suppressEmit by remember { mutableIntStateOf(0) }

    // 중앙 값이 바뀌는 즉시 커밋
    LaunchedEffect(size) {
        snapshotFlow { findCenteredAbsIndex(state) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { abs ->
                val idx = floorMod(abs, size)
                previewIndex = idx

                if (suppressEmit == 1) {
                    committedAbs = abs
                    return@collect
                }

                val diff = abs - committedAbs
                if (diff == 0) return@collect

                val dir = if (diff > 0) +1 else -1
                repeat(kotlin.math.abs(diff)) { k ->
                    val stepAbs = committedAbs + dir * (k + 1)
                    val stepIdx = floorMod(stepAbs, size)
                    onStep(dir, stepIdx)
                }
                committedAbs = abs
            }
    }

    // 외부 selectedIndex 변화(분 carry 등) 동기화
    LaunchedEffect(selectedIndex) {
        if (state.isScrollInProgress) return@LaunchedEffect

        val centered = findCenteredAbsIndex(state) ?: return@LaunchedEffect
        val currentIdx = floorMod(centered, size)
        if (currentIdx == selectedIndex) return@LaunchedEffect

        val targetAbs = centered + (selectedIndex - currentIdx)

        suppressEmit = 1
        state.animateScrollToItem(targetAbs)
        committedAbs = targetAbs
        previewIndex = selectedIndex
        suppressEmit = 0
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
    ) {
        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = centerPadding),
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(dampingConnection),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(count = Int.MAX_VALUE, key = { it }) { abs ->
                val idx = floorMod(abs, size)
                val isSelected = (idx == previewIndex)

                Text(
                    text = labelFor(idx),
                    style = if (!smallFont) t.displayMedium else t.headlineMedium,
                    color = if (isSelected) c.onBackground else c.onSurface.copy(alpha = 0.5f),
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