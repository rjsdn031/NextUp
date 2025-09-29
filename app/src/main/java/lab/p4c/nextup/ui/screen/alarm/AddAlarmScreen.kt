package lab.p4c.nextup.ui.screen.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lab.p4c.nextup.ui.widget.AlarmNameField
import lab.p4c.nextup.ui.widget.AlarmOptionsView
import lab.p4c.nextup.ui.widget.AlarmTimePicker
import lab.p4c.nextup.ui.widget.DaySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    navController: NavController,
    vm: AddAlarmViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("알람 추가") })
        },
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
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AlarmTimePicker(
                hour = ui.hour,
                minute = ui.minute,
                onTimePicked = { h, m -> vm.updateTime(h, m) }
            )

            DaySelector(
                selectedDays = ui.repeatDays,
                onChange = vm::updateDays
            )

            ListItem(
                headlineContent = { Text("공휴일엔 알람 끄기") },
                supportingContent = { Text(if (ui.skipHolidays) "사용" else "사용 안 함") },
                trailingContent = {
                    Switch(checked = ui.skipHolidays, onCheckedChange = vm::toggleSkipHolidays)
                }
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            AlarmNameField(
                value = androidx.compose.ui.text.input.TextFieldValue(ui.label),
                onValueChange = { vm.updateLabel(it.text) }
            )

            AlarmOptionsView(
                alarmSoundEnabled = ui.alarmSoundEnabled,
                selectedRingtoneName = ui.ringtoneName,
                onAlarmSoundToggle = vm::toggleAlarmSound,
                onSelectSound = {
                    // 실제 사운드 선택 연결(샘플 값)
                    vm.selectSound("Classic Bell", "assets/sounds/test_sound.mp3")
                },
                isPreviewing = ui.isPreviewing,
                onTogglePreview = vm::togglePreview,

                vibrationEnabled = ui.vibration,
                onVibrationToggle = vm::toggleVibration,

                snoozeLabel = "매 ${ui.snoozeInterval}분, 최대 ${ui.maxSnoozeCount}회",
                onSelectSnooze = {
                    // 다이얼로그로 대체 가능. 샘플: 간단 순환
                    val nextInterval = when (ui.snoozeInterval) { 3 -> 5; 5 -> 10; else -> 3 }
                    val nextCount = if (ui.maxSnoozeCount == 3) 5 else 3
                    vm.selectSnooze(nextInterval, nextCount)
                },

                volume = ui.volume,
                onSelectVolume = vm::updateVolume,

                fadeEnabled = ui.fadeSeconds > 0,
                onFadeToggle = vm::toggleFade,

                loop = ui.loop,
                onLoopToggle = vm::toggleLoop,

                snoozeEnabled = ui.snoozeEnabled,
                onToggleSnooze = vm::toggleSnoozeEnabled,
            )

            Spacer(Modifier.height(8.dp))
            Text(
                ui.nextTriggerText ?: "다음 울림 시간이 계산됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
