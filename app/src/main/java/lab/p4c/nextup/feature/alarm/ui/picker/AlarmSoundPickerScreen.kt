package lab.p4c.nextup.feature.alarm.ui.picker

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.infra.loader.SystemTone
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPreviewPlayer

/**
 * Alarm sound picker screen.
 *
 * Behavior:
 * - Selecting an item only updates UI selection state.
 * - The selection is applied only when the user presses the bottom "선택" button.
 *
 * @param systemSounds system alarm tones loaded from the device.
 * @param onSelect called when the user confirms selection via the bottom button.
 */
@Composable
fun AlarmSoundPickerScreen(
    modifier: Modifier = Modifier,
    systemSounds: List<SystemTone>,
    onSelect: (AlarmSound, String) -> Unit,
) {
    val ctx = LocalContext.current
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    var selected by remember { mutableStateOf<SelectedSound?>(null) }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            runCatching {
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            }

            val name = UriNameResolver.displayName(ctx, uri) ?: "사용자 파일"
            selected = SelectedSound(
                sound = AlarmSound.Custom(uri.toString()),
                title = name
            )
        }
    )

    val preview = remember { AlarmPreviewPlayer(ctx.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { preview.stop() }
    }

    LaunchedEffect(selected?.sound) {
        val sound = selected?.sound ?: run {
            preview.stop()
            return@LaunchedEffect
        }

        runCatching { preview.play(sound) }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("앱 기본 알람음", style = t.titleMedium, color = c.onSurface)
            }

            items(
                items = defaultAppSounds,
                key = { it.title }
            ) { item ->
                SoundRow(
                    title = item.title,
                    selected = selected?.title == item.title && selected?.sound == item.sound,
                    onClick = {
                        val next = SelectedSound(item.sound, item.title)
                        selected = if (selected == next) null else next
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = c.primary
                        )
                    },
                    containerColor = c.surface
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text("사용자 파일", style = t.titleMedium, color = c.onSurface)
            }

            item {
                SoundRow(
                    title = selected?.takeIf { it.sound is AlarmSound.Custom }?.title ?: "파일에서 선택",
                    selected = selected?.sound is AlarmSound.Custom,
                    onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = c.primary
                        )
                    },
                    containerColor = c.surface
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text("시스템 기본 알람음", style = t.titleMedium, color = c.onSurface)
            }

            items(
                items = systemSounds,
                key = { it.title }
            ) { item ->
                SoundRow(
                    title = item.title,
                    selected = selected?.title == item.title && selected?.sound == item.sound,
                    onClick = {
                        val next = SelectedSound(item.sound, item.title)
                        selected = if (selected == next) null else next
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = c.primary
                        )
                    },
                    containerColor = c.surface
                )
            }
        }

        BottomConfirmBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            enabled = selected != null,
            onConfirm = {
                val s = selected ?: return@BottomConfirmBar
                onSelect(s.sound, s.title)
            }
        )
    }
}

@Composable
private fun SoundRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    ListItem(
        headlineContent = { Text(title, style = t.bodyLarge) },
        leadingContent = leadingIcon,
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickableThrottle { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = c.onSurface
        )
    )
}

@Composable
private fun BottomConfirmBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onConfirm: () -> Unit,
) {
    val c = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = c.outline.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))

        ThrottleButton(
            onClick = onConfirm,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("선택")
        }
    }
}

private data class SelectedSound(
    val sound: AlarmSound,
    val title: String,
)