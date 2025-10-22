package lab.p4c.nextup.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.alarm.ui.list.AlarmListScreen
import lab.p4c.nextup.feature.alarm.ui.add.AddAlarmScreen
import lab.p4c.nextup.feature.alarm.ui.edit.EditAlarmScreen
import lab.p4c.nextup.feature.settings.ui.AlarmSettingsScreen
import lab.p4c.nextup.feature.usage.ui.UsageDetailRoute
import lab.p4c.nextup.feature.usage.ui.UsageStatsScreen
import lab.p4c.nextup.feature.usage.ui.UsageStatsSharedViewModel

private object Routes {
    const val Settings = "settings"
    const val AlarmList = "alarm/list"
    const val Add = "add"
    const val Edit = "edit/{id}"
    fun edit(id: Int) = "edit/$id"

    // usage 서브그래프
    const val UsageGraph = "usage_graph"
    const val UsageList  = "usage"                 // 리스트
    const val UsageDetail = "usage/detail/{pkg}"   // 상세
    fun usageDetail(pkg: String) = "usage/detail/$pkg"
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
        composable(Routes.Settings) {
            AlarmSettingsScreen(navController)
        }
        composable(
            route = Routes.Edit,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            EditAlarmScreen(alarmId = id, navController = navController)
        }

        // ✅ usage 서브그래프: 리스트/상세가 같은 부모 entry에 스코프된 VM 공유
        navigation(
            route = Routes.UsageGraph,
            startDestination = Routes.UsageList
        ) {
            composable(Routes.UsageList) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.UsageGraph)
                }
                val sharedVm: UsageStatsSharedViewModel = hiltViewModel(parentEntry)
                UsageStatsScreen(
                    navController = navController,
                    sharedVm = sharedVm
                )
            }
            composable(
                route = Routes.UsageDetail,
                arguments = listOf(navArgument("pkg") { type = NavType.StringType })
            ) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.UsageGraph)
                }
                val sharedVm: UsageStatsSharedViewModel = hiltViewModel(parentEntry)

                val pkg = entry.arguments?.getString("pkg") ?: return@composable
                UsageDetailRoute(
                    appPackage = pkg,
                    onBack = { navController.popBackStack() },
                    sharedVm = sharedVm
                )
            }
        }
    }
}
