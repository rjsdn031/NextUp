package lab.p4c.nextup.feature.settings.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import lab.p4c.nextup.app.ui.components.ThrottleIconButton
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.platform.permission.AccessibilityPermission
import lab.p4c.nextup.platform.permission.BatteryOptimizationPermission
import lab.p4c.nextup.platform.permission.ExactAlarmPermission
import lab.p4c.nextup.platform.permission.MicrophonePermission
import lab.p4c.nextup.platform.permission.NotificationPermission
import lab.p4c.nextup.platform.permission.OverlayPermission
import lab.p4c.nextup.platform.permission.SpeechSettingsIntents
import lab.p4c.nextup.platform.permission.UsageAccessPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(navController: NavController) {
    val ctx = LocalContext.current

    // 각각의 현재 상태
    var exactGranted by remember { mutableStateOf(ExactAlarmPermission.canSchedule(ctx)) }
    var overlayGranted by remember { mutableStateOf(OverlayPermission.canDraw(ctx)) }
    var a11yEnabled by remember { mutableStateOf(AccessibilityPermission.isEnabled(ctx)) }
    var usageGranted by remember { mutableStateOf(UsageAccessPermission.isGranted(ctx)) }
    var notifGranted by remember { mutableStateOf(NotificationPermission.isGranted(ctx)) }
    var batteryIgnored by remember { mutableStateOf(BatteryOptimizationPermission.isIgnoring(ctx)) }
    var micGranted by remember { mutableStateOf(MicrophonePermission.isGranted(ctx)) }

    // 설정 갔다가 돌아오면 다시 체크
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                exactGranted = ExactAlarmPermission.canSchedule(ctx)
                overlayGranted = OverlayPermission.canDraw(ctx)
                a11yEnabled = AccessibilityPermission.isEnabled(ctx)
                usageGranted = UsageAccessPermission.isGranted(ctx)
                notifGranted = NotificationPermission.isGranted(ctx)
                batteryIgnored = BatteryOptimizationPermission.isIgnoring(ctx)
                micGranted = MicrophonePermission.isGranted(ctx)
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
                    ThrottleIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("실험 정보 입력") },
                        supportingContent = { Text("실험자 이름, 나이, 성별을 입력하세요") },
                        modifier = Modifier.clickableThrottle {
                            navController.navigate("experimentInfo")
                        }
                    )
                }
            }

            item {
                PermissionCard(
                    title = "정확한 알람",
                    granted = exactGranted,
                    grantedText = "허용됨",
                    deniedText = "허용 필요 (Android 12+)",
                    onClick = { ExactAlarmPermission.request(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "오버레이 권한",
                    granted = overlayGranted,
                    grantedText = "허용됨",
                    deniedText = "앱 위에 그리기 필요",
                    onClick = { OverlayPermission.request(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "접근성 서비스",
                    granted = a11yEnabled,
                    grantedText = "활성화됨",
                    deniedText = "비활성화됨 (차단 기능 사용 시 필요)",
                    onClick = { AccessibilityPermission.openSettings(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "사용량 접근",
                    granted = usageGranted,
                    grantedText = "허용됨",
                    deniedText = "앱 사용 감지를 위해 필요",
                    onClick = { UsageAccessPermission.request(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "알림 허용",
                    granted = notifGranted,
                    grantedText = "허용됨",
                    deniedText = "알람 알림/풀스크린 표시 위해 권장",
                    onClick = { NotificationPermission.openSettings(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "배터리 최적화 제외",
                    granted = batteryIgnored,
                    grantedText = "제외됨",
                    deniedText = "절전 중 알람 지연 방지에 권장",
                    onClick = { BatteryOptimizationPermission.openOptimizationSettings(ctx) }
                )
            }

            item {
                PermissionCard(
                    title = "마이크 권한",
                    granted = micGranted,
                    grantedText = "허용됨",
                    deniedText = "음성 인식 해제 기능 사용 시 필요",
                    onClick = {
                        (ctx as? Activity)?.let {
                            if (!MicrophonePermission.isGranted(it)) {
                                MicrophonePermission.request(it)
                            } else {
                                MicrophonePermission.openSettings(it)
                            }
                        }
                    }
                )
            }

            item {
                val c = MaterialTheme.colorScheme
                val x = NextUpThemeTokens.colors

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = c.surface,
                        contentColor = c.onSurface
                    )
                ) {
                    ListItem(
                        headlineContent = { Text("오프라인 음성(한국어)") },
                        supportingContent = {
                            Text(
                                text = "오프라인 한국어 데이터 설치/관리",
                                color = x.textSecondary
                            )
                        },
                        trailingContent = {
                            Button(
                                onClick = { SpeechSettingsIntents.openOfflineSpeechSettings(ctx) }
                            ) {
                                Text("설정")
                            }
                        }
                    )
                }
            }


            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("차단할 앱 선택") },
                        supportingContent = { Text("오버레이로 차단할 앱을 선택하세요") },
                        modifier = Modifier.clickableThrottle {
                            navController.navigate("blockTargets")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    granted: Boolean,
    grantedText: String,
    deniedText: String,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors

    val statusColor = when {
        granted -> x.textMuted
        else -> x.primaryMuted
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = c.surface,
            contentColor = c.onSurface
        )
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = {
                Text(
                    text = if (granted) grantedText else deniedText,
                    color = statusColor
                )
            },
            trailingContent = {
                if (!granted) {
                    Button(onClick = onClick) {
                        Text("설정")
                    }
                }
            }
        )
    }
}

