package lab.p4c.nextup.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navDeepLink
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.ui.list.AlarmListScreen
import lab.p4c.nextup.feature.alarm.ui.add.AddAlarmScreen
import lab.p4c.nextup.feature.alarm.ui.edit.EditAlarmScreen
import lab.p4c.nextup.feature.alarm.ui.picker.AlarmSoundPickerRoute
import lab.p4c.nextup.feature.settings.ui.AlarmSettingsScreen
import lab.p4c.nextup.feature.settings.ui.BlockTargetSettingsRoute
import lab.p4c.nextup.feature.settings.ui.experiment.ExperimentInfoScreen
import lab.p4c.nextup.feature.survey.ui.SurveyScreen
import lab.p4c.nextup.feature.survey.ui.SurveyScreenViewModel
import lab.p4c.nextup.feature.usage.ui.UsageDetailRoute
import lab.p4c.nextup.feature.usage.ui.UsageStatsScreen

private object Routes {
    const val SETTINGS = "settings"
    const val TARGET_SETTINGS = "blockTargets"
    const val EXPERIMENT_INFO = "experimentInfo"
    const val ALARMLIST = "alarm/list"
    const val ADD = "add"
    const val SOUND_PICKER = "alarm/sound-picker"
    const val EDIT = "edit/{id}"
    fun edit(id: Int) = "edit/$id"

    // usage 서브그래프
    const val USAGEGRAPH = "usage_graph"
    const val USAGELIST = "usage"                 // 리스트
    const val USAGEDETAIL = "usage/detail/{pkg}?startMs={startMs}&endMs={endMs}"
    fun usageDetail(pkg: String, startMs: Long, endMs: Long): String {
        return "usage/detail/$pkg?startMs=$startMs&endMs=$endMs"
    }

    const val SURVEY = "survey"
    const val SURVEY_COMPLETE = "survey/complete"
    const val DEEPLINK_SURVEY = "app://nextup/survey" // 알림 URI
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ALARMLIST
    ) {
        composable(Routes.ALARMLIST) {
            AlarmListScreen(navController = navController)
        }
        composable(Routes.ADD) {
            AddAlarmScreen(navController = navController)
        }
        composable(Routes.SOUND_PICKER) {
            AlarmSoundPickerRoute(
                onSelect = { sound, title ->
                    val prev = navController.previousBackStackEntry ?: return@AlarmSoundPickerRoute
                    val handle = prev.savedStateHandle

                    when (sound) {
                        is AlarmSound.Asset -> {
                            handle["selectedSoundType"] = "asset"
                            handle["selectedSoundValue"] = sound.resName
                        }

                        is AlarmSound.System -> {
                            handle["selectedSoundType"] = "system"
                            handle["selectedSoundValue"] = sound.uri
                        }

                        is AlarmSound.Custom -> {
                            handle["selectedSoundType"] = "custom"
                            handle["selectedSoundValue"] = sound.uri
                        }
                    }

                    handle["selectedSoundTitle"] = title

                    navController.popBackStack()
                }
            )
        }
        composable(Routes.SETTINGS) {
            AlarmSettingsScreen(navController)
        }
        composable(Routes.TARGET_SETTINGS) {
            BlockTargetSettingsRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.EXPERIMENT_INFO) {
            ExperimentInfoScreen(navController)
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            EditAlarmScreen(alarmId = id, navController = navController)
        }

        // 설문: 딥링크로 진입 가능
        composable(
            route = Routes.SURVEY,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.DEEPLINK_SURVEY + "?source={source}" }
            ),
            arguments = listOf(
                navArgument("source") {
                    type = NavType.StringType; defaultValue = ""; nullable = true
                }
            )
        ) { entry ->
            val vm: SurveyScreenViewModel = hiltViewModel()

            LaunchedEffect(entry.id) {
                vm.onEnter()
            }

            SurveyScreen(
                vm = vm,
                onComplete = {
                    navController.navigate(Routes.ALARMLIST) {
                        popUpTo(Routes.SURVEY) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // (선택) 설문 완료 화면
        composable(Routes.SURVEY_COMPLETE) {
            // 가벼운 "완료" UI 혹은 토스트/스낵바로 대체 가능
            // CompleteScreen(onDone = { navController.navigate(Routes.ALARMLIST) { popUpTo(0) } })
        }

        navigation(
            route = Routes.USAGEGRAPH,
            startDestination = Routes.USAGELIST
        ) {
            composable(Routes.USAGELIST) {
                UsageStatsScreen(navController = navController)
            }

            composable(
                route = Routes.USAGEDETAIL,
                arguments = listOf(
                    navArgument("pkg") { type = NavType.StringType },
                    navArgument("startMs") { type = NavType.StringType },
                    navArgument("endMs") { type = NavType.StringType }
                )
            ) {
                UsageDetailRoute(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
