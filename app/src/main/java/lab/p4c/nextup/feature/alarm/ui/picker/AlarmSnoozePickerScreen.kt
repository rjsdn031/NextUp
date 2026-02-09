package lab.p4c.nextup.feature.alarm.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.util.clickableThrottle

private data class IntervalOption(val minutes: Int, val label: String)
private data class CountOption(val count: Int, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSnoozePickerScreen(
    onBack: () -> Unit,
    onApply: (enabled: Boolean, interval: Int, maxCount: Int) -> Unit,
    initialEnabled: Boolean? = null,
    initialInterval: Int? = null,
    initialMaxCount: Int? = null,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val intervalOptions = remember {
        listOf(
            IntervalOption(5, "5분"),
            IntervalOption(10, "10분"),
            IntervalOption(15, "15분"),
            IntervalOption(30, "30분"),
        )
    }
    // “계속 반복”은 내부 값으로 크게 잡아도 되고(Int.MAX_VALUE),
    // 도메인에서 이미 의미가 정해져 있으면 그 값에 맞춰주면 됨.
    val countOptions = remember {
        listOf(
            CountOption(3, "3회"),
            CountOption(5, "5회"),
            CountOption(Int.MAX_VALUE, "계속 반복"),
        )
    }

    var enabled by remember { mutableStateOf(initialEnabled ?: true) }
    var interval by remember { mutableIntStateOf(initialInterval ?: 5) }
    var maxCount by remember { mutableIntStateOf(initialMaxCount ?: Int.MAX_VALUE) }

    Scaffold(
        containerColor = c.background,
        contentColor = c.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("다시 울림") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.background,
                    titleContentColor = c.onBackground,
                    navigationIconContentColor = c.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = c.background,
                contentColor = c.onBackground,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThrottleOutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    ) { Text("취소") }

                    ThrottleButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onApply(enabled, interval, maxCount) }
                    ) { Text("적용") }
                }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 상단 “사용 중” 카드 (스크린샷 느낌)
            item {
                Surface(
                    color = c.surface,
                    contentColor = c.onSurface,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "사용 중",
                            style = t.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "간격",
                    style = t.labelLarge,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                Surface(
                    color = c.surface,
                    contentColor = c.onSurface,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        intervalOptions.forEachIndexed { idx, opt ->
                            SnoozeRadioRow(
                                label = opt.label,
                                selected = (interval == opt.minutes),
                                onSelect = { interval = opt.minutes }
                            )
                            if (idx != intervalOptions.lastIndex) HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                color = c.outline
                            )
                        }
                        // “직접 설정”이 필요하면 여기서 추가 Row를 넣고 Dialog로 분기하면 됨
                    }
                }
            }

            item {
                Text(
                    text = "반복",
                    style = t.labelLarge,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                Surface(
                    color = c.surface,
                    contentColor = c.onSurface,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        countOptions.forEachIndexed { idx, opt ->
                            SnoozeRadioRow(
                                label = opt.label,
                                selected = (maxCount == opt.count),
                                onSelect = { maxCount = opt.count }
                            )
                            if (idx != countOptions.lastIndex) {
                                HorizontalDivider(
                                    Modifier,
                                    DividerDefaults.Thickness,
                                    color = c.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnoozeRadioRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableThrottle { onSelect() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, color = c.onSurface)
    }
}
