package lab.p4c.nextup.feature.alarm.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lab.p4c.nextup.feature.alarm.ui.components.AlarmNameField
import lab.p4c.nextup.feature.alarm.ui.components.AlarmOptionsView
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTimePicker
import lab.p4c.nextup.feature.alarm.ui.components.DaySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    alarmId: Int,
    navController: NavController,
    vm: EditAlarmViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(alarmId) { vm.load(alarmId) }

    BackHandler(enabled = true) {
        // 변경 감지 후 뒤로가기 정책 필요 시 다이얼로그로 확장 가능
        navController.popBackStack()
    }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.popBackStack() }
                    ) { Text("취소") }

                    Button(
                        modifier = Modifier.weight(2f),
                        enabled = ui.canSave && !ui.isBusy,
                        onClick = { vm.save { navController.popBackStack() } }
                    ) { Text("저장") }
                }
            }
        }
    ) { inner ->
        if (!ui.loaded) {
            Box(
                Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(horizontal = 16.dp)
                    .padding(top = 30.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlarmTimePicker(
                    hour = ui.hour,
                    minute = ui.minute, onTimeChange = { h, m -> vm.updateTime(h, m) }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            ui.nextTriggerText ?: "다음 울림 시간이 계산됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    item {
                        DaySelector(
                            selectedDays = ui.repeatDays,
                            onChange = vm::updateDays
                        )
                    }

                    item {
                        ListItem(
                            headlineContent = { Text("공휴일엔 알람 끄기") },
                            supportingContent = { Text(if (ui.skipHolidays) "사용" else "사용 안 함") },
                            trailingContent = {
                                Switch(
                                    checked = ui.skipHolidays,
                                    onCheckedChange = vm::toggleSkipHolidays
                                )
                            }
                        )
                    }

                    item {
                        AlarmNameField(
                            value = TextFieldValue(ui.label),
                            onValueChange = { vm.updateLabel(it.text) }
                        )
                    }


                    item {
                        AlarmOptionsView(
                            alarmSoundEnabled = ui.alarmSoundEnabled,
                            selectedRingtoneName = ui.ringtoneName,
                            onAlarmSoundToggle = vm::toggleAlarmSound,
                            onSelectSound = {
                                // 실제 사운드 선택 연결 필요 시 다이얼로그로 교체
                                vm.selectSound("Classic Bell", "assets/sounds/test_sound.mp3")
                            },
                            isPreviewing = ui.isPreviewing,
                            onTogglePreview = vm::togglePreview,

                            vibrationEnabled = ui.vibration,
                            onVibrationToggle = vm::toggleVibration,

                            // 스누즈 관련
                            snoozeLabel = "매 ${ui.snoozeInterval}분, 최대 ${ui.maxSnoozeCount}회",
                            onSelectSnooze = {
                                val nextInterval = when (ui.snoozeInterval) {
                                    3 -> 5; 5 -> 10; else -> 3
                                }
                                val nextCount = if (ui.maxSnoozeCount == 3) 5 else 3
                                vm.selectSnooze(nextInterval, nextCount)
                            },

                            // 사운드 부가 옵션
                            volume = ui.volume,
                            onSelectVolume = vm::updateVolume,

                            snoozeEnabled = ui.snoozeEnabled,
                            onToggleSnooze = vm::toggleSnoozeEnabled,

                            )
                    }
                }
            }
        }
    }
}
