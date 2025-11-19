package lab.p4c.nextup.feature.alarm.ui.picker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.ui.util.rememberSystemAlarmSounds

/**
 * Route-level composable for selecting alarm sounds.
 * This screen wraps AlarmSoundPickerScreen with a top app bar and exposes the onSelect callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSoundPickerRoute(
    onSelect: (AlarmSound, String) -> Unit
) {
    val context = LocalContext.current

    val systemSounds = rememberSystemAlarmSounds(context)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("알람음 선택") }
            )
        }
    ) { innerPadding ->
        AlarmSoundPickerScreen(
            modifier = Modifier.padding(innerPadding),
            systemSounds = systemSounds,
            onSelect = onSelect
        )
    }
}