package lab.p4c.nextup.feature.alarm.ui.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.alarm.infra.scheduler.AlarmReceiver
import lab.p4c.nextup.feature.alarm.ui.components.AlarmFAB
import lab.p4c.nextup.feature.alarm.ui.components.AlarmListView
import lab.p4c.nextup.feature.alarm.ui.components.AlarmTopBar
import lab.p4c.nextup.feature.alarm.ui.ringing.AlarmRingingActivity
import lab.p4c.nextup.feature.survey.infra.scheduler.AndroidSurveyReminderScheduler
import java.time.ZoneId


@Composable
fun AlarmListScreen(
    navController: NavController,
    vm: AlarmListViewModel = hiltViewModel()
) {
    val alarms by vm.alarms.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Box {
                AlarmTopBar(onMenuClick = { expanded = true })
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("설정") },
                        onClick = {
                            expanded = false
                            navController.navigate("settings")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("앱 사용 통계") },
                        onClick = {
                            expanded = false
                            navController.navigate("usage")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("푸시알림 테스트") },
                        onClick = {
                            expanded = false

                            val zdt = now.plusMinutes(2).withSecond(0).withNano(0)
                            AndroidSurveyReminderScheduler(ctx).scheduleAt(zdt)
                            Toast.makeText(
                                ctx,
                                "테스트 알림을 예약했습니다: ${zdt.toLocalTime()}",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    )
                }
            }
        },
        floatingActionButton = {
            AlarmFAB(onClick = { navController.navigate("add") })
//            AlarmFAB(onClick = {
//                val id = alarms.firstOrNull()?.id ?: 1  // 임시 id
//                ctx.startActivity(
//                    Intent(ctx, AlarmRingingActivity::class.java)
//                        .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
//                )
//            })
        }
    ) { padding ->
        AlarmListView(
            alarms = alarms,
            now = now,
            onToggle = { a, enabled -> vm.onToggle(a, enabled) },
            onDelete = { id -> vm.onDelete(id) },
            onTap = { a, _ -> navController.navigate("edit/${a.id}") },
            onAdd = {},
            onUpdate = {},
            computeNextMillis = vm::computeNextMillis,
            formatNext = vm::formatNext,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
        // Add Delete Update는 List화면에서 조작이 이뤄지지 않는다. 수정 요망
    }
}