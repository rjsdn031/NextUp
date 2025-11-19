package lab.p4c.nextup.feature.alarm.ui.util

import androidx.compose.runtime.Composable

@Composable
fun rememberSystemAlarmSounds(context: android.content.Context) =
    androidx.compose.runtime.remember {
        lab.p4c.nextup.feature.alarm.infra.loader.SystemRingtoneLoader
            .loadSystemAlarms(context)
    }