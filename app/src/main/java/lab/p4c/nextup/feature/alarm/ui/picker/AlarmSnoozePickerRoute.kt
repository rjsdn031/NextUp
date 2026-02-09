package lab.p4c.nextup.feature.alarm.ui.picker

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AlarmSnoozePickerRoute(
    navController: NavController
) {
    val prevHandle = navController.previousBackStackEntry?.savedStateHandle

    val initialEnabled = prevHandle?.get<Boolean>("snoozeInitialEnabled")
    val initialInterval = prevHandle?.get<Int>("snoozeInitialInterval")
    val initialMaxCount = prevHandle?.get<Int>("snoozeInitialMaxCount")

    prevHandle?.remove<Boolean>("snoozeInitialEnabled")
    prevHandle?.remove<Int>("snoozeInitialInterval")
    prevHandle?.remove<Int>("snoozeInitialMaxCount")

    AlarmSnoozePickerScreen(
        onBack = { navController.popBackStack() },
        onApply = { enabled, interval, maxCount ->
            val targetHandle = navController.previousBackStackEntry?.savedStateHandle

            targetHandle?.set("selectedSnoozeEnabled", enabled)
            targetHandle?.set("selectedSnoozeInterval", interval)
            targetHandle?.set("selectedSnoozeMaxCount", maxCount)

            navController.popBackStack()
        },
        initialEnabled = initialEnabled,
        initialInterval = initialInterval,
        initialMaxCount = initialMaxCount,
    )
}
