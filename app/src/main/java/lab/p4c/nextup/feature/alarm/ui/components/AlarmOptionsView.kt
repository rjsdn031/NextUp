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
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Column {
        /* 알람음 */
        ListItem(
            headlineContent = { Text("알람음", style = t.titleMedium) },
            supportingContent = {
                Text(
                    if (alarmSoundEnabled) selectedRingtoneName else "사용 안 함",
                    color = c.onSurfaceVariant
                )
            },
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        enabled = alarmSoundEnabled,
                        onClick = onTogglePreview,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (alarmSoundEnabled) c.onSurface else c.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            if (isPreviewing) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "미리듣기"
                        )
                    }
                    IconButton(
                        enabled = alarmSoundEnabled,
                        onClick = onSelectSound,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (alarmSoundEnabled) c.onSurface else c.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "알람음 선택")
                    }
                    Switch(
                        checked = alarmSoundEnabled,
                        onCheckedChange = onAlarmSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.onPrimary,
                            checkedTrackColor = c.primary,
                            uncheckedThumbColor = c.outline,
                            uncheckedTrackColor = c.background
                        )
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = c.surface,
                headlineColor = c.onSurface,
                supportingColor = c.onSurfaceVariant
            )
        )

        /* 볼륨 */
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("볼륨 ${(volume * 100).toInt()}%", style = t.bodyMedium, color = c.onSurface)
            Slider(
                value = volume,
                onValueChange = onSelectVolume,
                colors = SliderDefaults.colors(
                    thumbColor = c.primary,
                    activeTrackColor = c.primary,
                    inactiveTrackColor = c.outline.copy(alpha = 0.4f)
                )
            )
        }

        /* 진동 */
        ListItem(
            headlineContent = { Text("진동", style = t.titleMedium) },
            supportingContent = {
                Text(if (vibrationEnabled) "사용" else "사용 안 함", color = c.onSurfaceVariant)
            },
            trailingContent = {
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationToggle,
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

        /* 스누즈 */
        ListItem(
            headlineContent = { Text("다시 울림", style = t.titleMedium) },
            supportingContent = {
                Text(
                    if (snoozeEnabled) snoozeLabel else "사용 안 함",
                    color = c.onSurfaceVariant
                )
            },
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        enabled = snoozeEnabled,
                        onClick = onSelectSnooze,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (snoozeEnabled) c.onSurface else c.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "스누즈 설정")
                    }
                    Switch(
                        checked = snoozeEnabled,
                        onCheckedChange = onToggleSnooze,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.onPrimary,
                            checkedTrackColor = c.primary,
                            uncheckedThumbColor = c.outline,
                            uncheckedTrackColor = c.background
                        )
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = c.background,
                headlineColor = c.onBackground,
                supportingColor = c.onSurfaceVariant
            )
        )
    }
}
