package lab.p4c.nextup.feature.alarm.ui.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.MenuDefaults.itemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lab.p4c.nextup.app.time.SystemTimeProvider
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.core.common.time.dayOfWeekToKor
import lab.p4c.nextup.core.common.time.formatTimeOfDay
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.feature.alarm.ui.components.AlarmAddTile
import lab.p4c.nextup.feature.alarm.ui.components.AlarmFAB
import lab.p4c.nextup.feature.alarm.ui.components.AlarmHeader
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTile
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTopBar
import lab.p4c.nextup.feature.alarm.ui.components.AlarmListMenu
import lab.p4c.nextup.feature.alarm.ui.components.SurveyLinkSection
import lab.p4c.nextup.feature.alarm.ui.ringing.AlarmRingingActivity
import lab.p4c.nextup.feature.survey.infra.scheduler.AndroidSurveyReminderScheduler
import java.time.Clock
import java.time.ZoneId


@Composable
fun AlarmListScreen(
    navController: NavController,
    vm: AlarmListViewModel = hiltViewModel()
) {
    val alarmsState by vm.alarms.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    val c = MaterialTheme.colorScheme

    val alarms: List<Alarm> = alarmsState ?: emptyList()

    Scaffold(
        containerColor = c.background,
        contentColor = c.onBackground,
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Box {
                AlarmTopBar(onMenuClick = { expanded = true })
                AlarmListMenu(
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    onNavigateSettings = { navController.navigate("settings") },
                    onNavigateUsage = { navController.navigate("usage") },
                    onTestSurveyReminder = { zdt ->
                        vm.scheduleTestSurveyReminder()
                        Toast.makeText(
                            ctx, "테스트 알림을 예약했습니다: ${zdt.toLocalTime()}", Toast.LENGTH_SHORT
                        ).show()
                    },
                    now = now
                )
            }
        },
        bottomBar = { SurveyLinkSection({ navController.navigate("survey") }) },
//        floatingActionButton = {
//            AlarmFAB(onClick = { navController.navigate("add") })
//            AlarmFAB(onClick = {
//                val id = alarms.firstOrNull()?.id ?: 1  // 임시 id
//                ctx.startActivity(
//                    Intent(ctx, AlarmRingingActivity::class.java)
//                        .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
//                )
//            })
//        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val nextMessage = remember(alarms, now) {
                val nextMillis =
                    alarms.asSequence()
                        .filter { it.enabled }
                        .map { vm.computeNextMillis(it, now) }
                        .minOrNull()

                nextMillis?.let { vm.formatNext(it) } ?: "설정된 다음 알람이 없습니다"
            }

            AlarmHeader(now = now, nextAlarmMessage = nextMessage)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (alarms.isEmpty()) {
                    item(key = "add") {
                        AlarmAddTile(
                            onClick = {
                                if (vm.canOpenAddAlarm()) {
                                    navController.navigate("add")
                                } else {
                                    Toast.makeText(ctx, "알람 추가를 위해 ‘정확한 알람’ 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                                    navController.navigate("settings")
                                }
                            }
                        )
                    }
                }

                itemsIndexed(alarms, key = { _, a -> a.id }) { _, alarm ->
                    val timeText = formatTimeOfDay(alarm.hour, alarm.minute)
                    val days = alarm.days.map { dayOfWeekToKor(it) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableThrottle { navController.navigate("edit/${alarm.id}") }
                    ) {
                        AlarmTile(
                            time = timeText,
                            days = days,
                            enabled = alarm.enabled,
                            onToggle = { checked -> vm.onToggle(alarm, checked) },
                            fixed = (alarm.id == 1),
                            onFixedToggle = {
                                Toast.makeText(ctx, "필수 알람은 해제할 수 없습니다", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    }
                }
            }
        }
        // Add Delete Update는 List화면에서 조작이 이뤄지지 않는다. 수정 요망
    }
}