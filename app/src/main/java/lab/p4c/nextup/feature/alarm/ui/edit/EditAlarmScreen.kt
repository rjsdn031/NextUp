package lab.p4c.nextup.feature.alarm.ui.edit

import android.widget.Toast
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
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.util.ToastThrottler
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.ui.components.AlarmNameField
import lab.p4c.nextup.feature.alarm.ui.components.AlarmOptionRow
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTimePicker
import lab.p4c.nextup.feature.alarm.ui.components.AlarmVolumeRow
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
    val toastThrottler = remember { ToastThrottler(minIntervalMs = 1000L) }

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

    BackHandler(enabled = true) {
        navController.popBackStack()
    }

    var labelField by remember(ui.label) {
        mutableStateOf(TextFieldValue(ui.label))
    }

    val snoozeEnabledFlow = savedState?.getStateFlow<Boolean?>("selectedSnoozeEnabled", null)
    val snoozeIntervalFlow = savedState?.getStateFlow<Int?>("selectedSnoozeInterval", null)
    val snoozeMaxCountFlow = savedState?.getStateFlow<Int?>("selectedSnoozeMaxCount", null)

    val pickedSnoozeEnabled by snoozeEnabledFlow?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val pickedSnoozeInterval by snoozeIntervalFlow?.collectAsState() ?: remember {
        mutableStateOf(
            null
        )
    }
    val pickedSnoozeMaxCount by snoozeMaxCountFlow?.collectAsState() ?: remember {
        mutableStateOf(
            null
        )
    }

    LaunchedEffect(pickedSnoozeEnabled, pickedSnoozeInterval, pickedSnoozeMaxCount) {
        val enabled = pickedSnoozeEnabled
        val interval = pickedSnoozeInterval
        val maxCount = pickedSnoozeMaxCount

        if (enabled != null) {
            vm.toggleSnoozeEnabled(enabled)
            savedState?.remove<Boolean>("selectedSnoozeEnabled")
        }

        // interval/maxCount는 세트로 왔을 때 반영
        if (interval != null && maxCount != null) {
            vm.selectSnooze(interval, maxCount)
            savedState?.remove<Int>("selectedSnoozeInterval")
            savedState?.remove<Int>("selectedSnoozeMaxCount")
        }
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
                            onChange = { days ->
                                if (isMandatory) {
                                    Toast.makeText(
                                        context,
                                        "필수 알람은 요일을 변경할 수 없습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    vm.updateDays(days)
                                }
                            }
                        )
                    }

                    if (!isMandatory) {
                        item {
                            AlarmOptionRow(
                                title = "공휴일엔 알람 끄기",
                                subtitle = if (ui.skipHolidays) "사용" else "사용 안 함",
                                checked = ui.skipHolidays,
                                enabled = !ui.isBusy,
                                onCheckedChange = { checked ->
                                    val ok = vm.toggleSkipHolidays(checked)
                                    if (!ok) {
                                        Toast.makeText(
                                            context,
                                            "필수 알람은 공휴일 설정을 변경할 수 없습니다",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }

                    item {
                        AlarmNameField(
                            value = labelField,
                            onValueChange = { labelField = it }
                        )
                    }


                    item {
                        val soundSummary = if (ui.alarmSoundEnabled) ui.ringtoneName else "사용 안 함"

                        AlarmOptionRow(
                            title = "알람음",
                            subtitle = soundSummary,
                            checked = ui.alarmSoundEnabled,
                            onCheckedChange = { enabled ->
                                val ok = vm.toggleAlarmSound(enabled)
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        "필수 알람은 알람음을 끌 수 없습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onClick = {
                                if (ui.alarmSoundEnabled) {
                                    { navController.navigate("alarm/sound-picker") }
                                } else null
                            },
                            enabled = !ui.isBusy
                        )
                    }

                    item {
                        AlarmVolumeRow(
                            volume = ui.volume,
                            enabled = ui.alarmSoundEnabled && !ui.isBusy,
                            onValueChange = { v ->
                                val ok = vm.updateVolume(v)
                                if (!ok) {
                                    toastThrottler.show(
                                        context = context,
                                        message = "필수 알람은 볼륨을 20% 미만으로 설정할 수 없습니다"
                                    )
                                }
                            }
                        )
                    }

                    item {
                        AlarmOptionRow(
                            title = "진동",
                            subtitle = if (ui.vibration) "사용" else "사용 안 함",
                            checked = ui.vibration,
                            enabled = !ui.isBusy,
                            onCheckedChange = { enabled ->
                                val ok = vm.toggleVibration(enabled)
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        "필수 알람은 진동을 끌 수 없습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    item {
                        val maxCountLabel =
                            if (ui.maxSnoozeCount == Int.MAX_VALUE) "계속 반복" else "최대 ${ui.maxSnoozeCount}회"

                        val snoozeSummary =
                            if (ui.snoozeEnabled) "매 ${ui.snoozeInterval}분, $maxCountLabel" else "사용 안 함"

                        AlarmOptionRow(
                            title = "다시 울림",
                            subtitle = snoozeSummary,
                            checked = ui.snoozeEnabled,
                            onCheckedChange = vm::toggleSnoozeEnabled,
                            onClick = {
                                if (ui.snoozeEnabled) {
                                    savedState?.set("snoozeInitialEnabled", true)
                                    savedState?.set("snoozeInitialInterval", ui.snoozeInterval)
                                    savedState?.set("snoozeInitialMaxCount", ui.maxSnoozeCount)
                                    navController.navigate("alarm/snooze-picker")
                                } else null
                            },
                            enabled = !ui.isBusy
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
