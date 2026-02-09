package lab.p4c.nextup.feature.alarm.ui.add

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPreviewPlayer
import lab.p4c.nextup.feature.alarm.ui.components.AlarmNameField
import lab.p4c.nextup.feature.alarm.ui.components.AlarmOptionsView
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTimePicker
import lab.p4c.nextup.feature.alarm.ui.components.DaySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    navController: NavController,
    vm: AddAlarmViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

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

    var labelField by remember(ui.label) {
        mutableStateOf(TextFieldValue(ui.label))
    }

    val snoozeEnabledFlow = savedState?.getStateFlow<Boolean?>("selectedSnoozeEnabled", null)
    val snoozeIntervalFlow = savedState?.getStateFlow<Int?>("selectedSnoozeInterval", null)
    val snoozeMaxCountFlow = savedState?.getStateFlow<Int?>("selectedSnoozeMaxCount", null)

    val pickedSnoozeEnabled by snoozeEnabledFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val pickedSnoozeInterval by snoozeIntervalFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val pickedSnoozeMaxCount by snoozeMaxCountFlow?.collectAsState() ?: remember { mutableStateOf(null) }

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

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is AddAlarmEvent.Saved -> {
                    navController.popBackStack()
                }
                is AddAlarmEvent.SaveFailed -> {
                    // Toast.makeText(LocalContext.current, event.message, LENGTH_SHORT).show()
                }
            }
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            },
        containerColor = c.background,
        contentColor = c.onBackground,
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
                    if (!ui.isFirstAlarm) {
                        ThrottleOutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { navController.popBackStack() }
                        ) { Text("취소") }
                    }

                    ThrottleButton(
                        modifier = Modifier.weight(1f),
                        enabled = ui.canSave && !ui.isBusy,
                        onClick = {
                            vm.updateLabel(labelField.text)
                            vm.save()
                        }
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    ) { inner ->
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
                minute = ui.minute,
                onTimeChange = { h, m -> vm.updateTime(h, m) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {  // (Calculated) Next Alarm
                    Text(
                        text = ui.nextTriggerText ?: "다음 울림 시간이 계산됩니다.",
                        style = t.bodySmall,
                        color = c.onSurfaceVariant
                    )
                }

                item {  // Weekly Loop
                    DaySelector(
                        selectedDays = ui.repeatDays,
                        onChange = { days ->
                            if (ui.isFirstAlarm) {
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

                if (!ui.isFirstAlarm) {
                    item {  // Turn off alarms on Public Holidays - restricted
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
                }

                item {  // Alarm Name
                    AlarmNameField(
                        value = labelField,
                        onValueChange = { labelField = it }
                    )
                }

                item {  // AlarmSound, Vibrate, Snooze
                    val maxCountLabel =
                        if (ui.maxSnoozeCount == Int.MAX_VALUE) "계속 반복"
                        else "최대 ${ui.maxSnoozeCount}회"
                    AlarmOptionsView(
                        alarmSoundEnabled = ui.alarmSoundEnabled,
                        selectedRingtoneName = ui.ringtoneName,
                        onAlarmSoundToggle = { enabled ->
                            val ok = vm.toggleAlarmSound(enabled)
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    "필수 알람은 알람음을 끌 수 없습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
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
                        onVibrationToggle = { enabled ->
                            val ok = vm.toggleVibration(enabled)
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    "필수 알람은 진동을 끌 수 없습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },

                        snoozeLabel = "매 ${ui.snoozeInterval}분, $maxCountLabel",
                        onSelectSnooze = {
                            savedState?.set("snoozeInitialEnabled", ui.snoozeEnabled)
                            savedState?.set("snoozeInitialInterval", ui.snoozeInterval)
                            savedState?.set("snoozeInitialMaxCount", ui.maxSnoozeCount)

                            navController.navigate("alarm/snooze-picker")
                        },

                        volume = ui.volume,
                        onSelectVolume = { v ->
                            val ok = vm.updateVolume(v)
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    "필수 알람은 볼륨을 20% 미만으로 설정할 수 없습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },

                        snoozeEnabled = ui.snoozeEnabled,
                        onToggleSnooze = vm::toggleSnoozeEnabled,
                    )
                }
            }
        }
    }
}
