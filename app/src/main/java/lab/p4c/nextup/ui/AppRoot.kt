package lab.p4c.nextup.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lab.p4c.nextup.ui.screen.alarm.AlarmListScreen
import lab.p4c.nextup.ui.screen.alarm.AddAlarmScreen
import lab.p4c.nextup.ui.screen.alarm.EditAlarmScreen
import lab.p4c.nextup.ui.screen.settings.AlarmSettingsScreen

private object Routes {
    const val Settings = "settings"
    const val AlarmList = "alarm/list"
    const val Add = "add"
    const val Edit = "edit/{id}"
    fun edit(id: Int) = "edit/$id"
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.AlarmList
    ) {
        composable(Routes.AlarmList) {
            AlarmListScreen(navController = navController)
        }
        composable(Routes.Add) {
            AddAlarmScreen(navController = navController)
        }
        composable(Routes.Settings) { AlarmSettingsScreen(navController) }
        composable(
            route = Routes.Edit,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            EditAlarmScreen(alarmId = id, navController = navController)
        }
    }
}
