package lab.p4c.nextup.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import lab.p4c.nextup.util.ExactAlarmPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(navController: NavController) {
    val ctx = LocalContext.current
    var exactGranted by remember { mutableStateOf(ExactAlarmPermission.canSchedule(ctx)) }

    // 설정 화면 다녀오면 재확인
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                exactGranted = ExactAlarmPermission.canSchedule(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                ListItem(
                    headlineContent = { Text("정확한 알람 권한") },
                    supportingContent = {
                        Text(if (exactGranted) "허용됨" else "허용 필요 (Android 12+)")
                    },
                    trailingContent = {
                        Button(
                            enabled = !exactGranted,
                            onClick = { ExactAlarmPermission.request(ctx) }
                        ) { Text("설정 열기") }
                    }
                )
            }

            // 필요하면 여기에 추가 옵션(예: 알림 권한, 배터리 최적화 제외 등)도 확장 가능
        }
    }
}