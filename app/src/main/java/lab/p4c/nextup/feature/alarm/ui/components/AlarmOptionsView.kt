package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlarmOptionsView(
    alarmSoundEnabled: Boolean,
    selectedRingtoneName: String,
    onAlarmSoundToggle: (Boolean) -> Unit,
    onSelectSound: () -> Unit,
    isPreviewing: Boolean,
    onTogglePreview: () -> Unit,

    vibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,

    snoozeLabel: String,
    onSelectSnooze: () -> Unit,

    volume: Float,
    onSelectVolume: (Float) -> Unit,

    fadeEnabled: Boolean = false,
    onFadeToggle: (Boolean) -> Unit = {},

    loop: Boolean = true,
    onLoopToggle: (Boolean) -> Unit = {},

    snoozeEnabled: Boolean,
    onToggleSnooze: (Boolean) -> Unit
) {
    Column {
        /* 알람음 */
        ListItem(
            headlineContent = { Text("알람음") },
            supportingContent = { Text(if (alarmSoundEnabled) selectedRingtoneName else "사용 안 함") },
            trailingContent = {
                Row {
                    IconButton(
                        enabled = alarmSoundEnabled,
                        onClick = onTogglePreview
                    ) {
                        Icon(if (isPreviewing) Icons.Default.PauseCircle else Icons.Default.PlayCircle, contentDescription = "미리듣기")
                    }
                    IconButton(
                        enabled = alarmSoundEnabled,
                        onClick = onSelectSound
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "알람음 선택")
                    }
                    Switch(checked = alarmSoundEnabled, onCheckedChange = onAlarmSoundToggle)
                }
            }
        )

        /* 볼륨 */
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("볼륨 ${(volume * 100).toInt()}%")
            Slider(value = volume, onValueChange = onSelectVolume)
        }

        /* 진동 */
        ListItem(
            headlineContent = { Text("진동") },
            supportingContent = { Text(if (vibrationEnabled) "사용" else "사용 안 함") },
            trailingContent = {
                Switch(checked = vibrationEnabled, onCheckedChange = onVibrationToggle)
            }
        )

        /* 스누즈 */
        ListItem(
            headlineContent = { Text("다시 울림") },
            supportingContent = {
                Text(if (snoozeEnabled) snoozeLabel else "사용 안 함")
            },
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        enabled = snoozeEnabled,
                        onClick = onSelectSnooze
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "스누즈 설정")
                    }
                    Switch(
                        checked = snoozeEnabled,
                        onCheckedChange = onToggleSnooze
                    )
                }
            }
        )
    }
}
