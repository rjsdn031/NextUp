package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lab.p4c.nextup.app.ui.util.clickableThrottle

/**
 * 스위치 기반 옵션 행.
 *
 * - onClick이 null이 아니면, Switch를 제외한 영역 탭을 통해 상세 설정 화면으로 이동할 수 있다.
 * - onClick이 null이면 단순 토글 행으로 동작한다.
 */
@Composable
fun AlarmOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val c = MaterialTheme.colorScheme

    val rowModifier = modifier
        .fillMaxWidth()
        .let { base ->
            if (onClick == null) base
            else base.clickableThrottle(enabled = enabled, onClick = onClick)
        }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = c.onPrimary,
                    checkedTrackColor = c.primary,
                    uncheckedThumbColor = c.outline,
                    uncheckedTrackColor = c.background
                )
            )
        },
        modifier = rowModifier,
        colors = ListItemDefaults.colors(
            containerColor = c.surface,
            headlineColor = c.onSurface,
            supportingColor = c.onSurfaceVariant
        )
    )
}