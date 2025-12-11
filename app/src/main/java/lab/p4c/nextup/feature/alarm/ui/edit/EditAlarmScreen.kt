package lab.p4c.nextup.feature.alarm.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPreviewPlayer
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

    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    val isMandatory = ui.id == 1

    LaunchedEffect(alarmId) { vm.load(alarmId) }

    val savedState = navController.currentBackStackEntry?.savedStateHandle

    val typeFlow = savedState?.getStateFlow<String?>("selectedSoundType", null)
    val valueFlow = savedState?.getStateFlow<String?>("selectedSoundValue", null)
    val titleFlow = savedState?.getStateFlow<String?>("selectedSoundTitle", null)

    val pickedType by typeFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val pickedValue by valueFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val pickedTitle by titleFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(pickedType, pickedValue) {
        val type = pickedType
        val value = pickedValue
        val title = pickedTitle ?: ""

        if (type != null && value != null) {
            val sound = when (type) {
                "asset" -> AlarmSound.Asset(value)
                "system" -> AlarmSound.System(value)
                "custom" -> AlarmSound.Custom(value)
                else -> return@LaunchedEffect
            }

            vm.selectSound(title, sound)

            savedState?.remove<String>("selectedSoundType")
            savedState?.remove<String>("selectedSoundValue")
            savedState?.remove<String>("selectedSoundTitle")
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preview = remember { AlarmPreviewPlayer(context) }

    DisposableEffect(Unit) {
        onDispose { preview.stop() }
    }

    BackHandler(enabled = true) {
        // 변경 감지 후 뒤로가기 정책 필요 시 다이얼로그로 확장 가능
        navController.popBackStack()
    }

    var labelField by remember(ui.label) {
        mutableStateOf(TextFieldValue(ui.label))
    }

    Scaffold(
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
                        onClick = { navController.popBackStack() }
                    ) { Text("취소") }

                    ThrottleButton(
                        modifier = Modifier.weight(1f),
                        enabled = ui.canSave && !ui.isBusy,
                        onClick = {
                            vm.updateLabel(labelField.text)
                            vm.save { navController.popBackStack() }
                        }
                    ) {
                        Text("저장")
                    }
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
                            text = ui.nextTriggerText ?: "다음 울림 시간이 계산됩니다.",
                            style = t.bodySmall,
                            color = c.onSurfaceVariant
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
                            supportingContent = {
                                Text(if (ui.skipHolidays) "사용" else "사용 안 함")
                            },
                            trailingContent = {
                                Switch(
                                    checked = ui.skipHolidays,
                                    onCheckedChange = vm::toggleSkipHolidays,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = c.onPrimary,
                                        checkedTrackColor = c.primary,
                                        uncheckedThumbColor = c.outline,
                                        uncheckedTrackColor = c.background
                                    )
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = c.surface,
                                headlineColor = c.onSurface,
                                supportingColor = c.onSurfaceVariant
                            )
                        )
                    }

                    item {
                        AlarmNameField(
                            value = labelField,
                            onValueChange = { labelField = it }
                        )
                    }


                    item {
                        AlarmOptionsView(
                            alarmSoundEnabled = ui.alarmSoundEnabled,
                            selectedRingtoneName = ui.ringtoneName,
                            onAlarmSoundToggle = vm::toggleAlarmSound,
                            onSelectSound = {
                                navController.navigate("alarm/sound-picker")
                            },
                            isPreviewing = ui.isPreviewing,
                            onTogglePreview = {
                                scope.launch {
                                    if (ui.isPreviewing) preview.stop()
                                    else preview.play(ui.sound)
                                    vm.togglePreview()
                                }
                            },

                            vibrationEnabled = ui.vibration,
                            onVibrationToggle = vm::toggleVibration,

                            // 스누즈 관련
                            snoozeLabel = "매 ${ui.snoozeInterval}분, 최대 ${ui.maxSnoozeCount}회",
                            onSelectSnooze = {
                                val nextInterval = when (ui.snoozeInterval) {
                                    1 -> 3
                                    3 -> 5
                                    5 -> 10
                                    10 -> 1
                                    else -> 1
                                }
                                val nextCount =
                                    if (ui.maxSnoozeCount == 3) 5 else if (ui.maxSnoozeCount == 5) 10 else 3
                                vm.selectSnooze(nextInterval, nextCount)
                            },

                            // 사운드 부가 옵션
                            volume = ui.volume,
                            onSelectVolume = vm::updateVolume,

                            snoozeEnabled = ui.snoozeEnabled,
                            onToggleSnooze = vm::toggleSnoozeEnabled,

                            )
                    }

                    if (!isMandatory) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text("알람 삭제", color = c.error)
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "삭제",
                                        tint = c.error
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickableThrottle(
                                        enabled = !ui.isBusy,
                                        onClick = { vm.delete { navController.popBackStack() } }
                                    ),
                                colors = ListItemDefaults.colors(
                                    containerColor = c.surface
                                )
                            )
                        }
                    }

                }
            }
        }
    }
}
