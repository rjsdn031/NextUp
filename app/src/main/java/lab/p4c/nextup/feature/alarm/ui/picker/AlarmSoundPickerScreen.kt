package lab.p4c.nextup.feature.alarm.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.infra.loader.SystemTone

/**
 * Main picker screen UI.
 * Displays:
 *  - Default app sounds (Asset)
 *  - System alarm tones (System)
 *  - "Pick from file" option (Custom)
 */
@Composable
fun AlarmSoundPickerScreen(
    modifier: Modifier = Modifier,
    systemSounds: List<SystemTone>,
    onSelect: (AlarmSound, String) -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        /* -------------------------------
         * 1) 앱 기본 알람음
         * ------------------------------- */
        item {
            Text("앱 기본 알람음", style = t.titleMedium, color = c.onSurface)
        }

        items(defaultAppSounds) { item ->
            ListItem(
                headlineContent = { Text(item.title, style = t.bodyLarge) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = c.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item.sound, item.title) },
                colors = ListItemDefaults.colors(
                    containerColor = c.surface
                )
            )
        }


        /* -------------------------------
         * 2) 시스템 알람음
         * ------------------------------- */
        item {
            Spacer(Modifier.height(20.dp))
            Text("시스템 기본 알람음", style = t.titleMedium, color = c.onSurface)
        }

        items(systemSounds) { tone ->
            ListItem(
                headlineContent = { Text(tone.title, style = t.bodyLarge) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = c.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(tone.sound, tone.title) },
                colors = ListItemDefaults.colors(
                    containerColor = c.surface
                )
            )
        }


        /* -------------------------------
         * 3) 사용자 파일 선택 (Custom)
         * ------------------------------- */
        item {
            Spacer(Modifier.height(20.dp))
            Text("사용자 파일", style = t.titleMedium, color = c.onSurface)
        }

        item {
            ListItem(
                headlineContent = { Text("파일에서 선택", style = t.bodyLarge) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = c.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // SAF 열기 로직은 다음 단계에서 구현
                        // onSelect(AlarmSound.Custom("content://..."), "사용자 파일")
                    },
                colors = ListItemDefaults.colors(
                    containerColor = c.surfaceVariant
                )
            )
        }
    }
}
