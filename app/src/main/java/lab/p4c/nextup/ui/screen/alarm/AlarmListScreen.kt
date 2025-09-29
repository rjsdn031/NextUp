package lab.p4c.nextup.ui.screen.alarm

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lab.p4c.nextup.data.scheduler.AlarmReceiver
import lab.p4c.nextup.ui.screen.ringing.AlarmRingingActivity
import lab.p4c.nextup.ui.widget.AlarmFAB
import lab.p4c.nextup.ui.widget.AlarmListView
import lab.p4c.nextup.ui.widget.AlarmTopBar


@Composable
fun AlarmListScreen(
    navController: NavController,
    vm: AlarmListViewModel = hiltViewModel()
) {
    val alarms by vm.alarms.collectAsState()
    val now by vm.now.collectAsState()
    val ctx = LocalContext.current

    Scaffold(
        topBar = { AlarmTopBar(onMenuClick = { navController.navigate("settings") }) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}