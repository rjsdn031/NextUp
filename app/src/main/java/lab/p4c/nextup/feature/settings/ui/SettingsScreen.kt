package lab.p4c.nextup.feature.settings.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import dagger.hilt.android.EntryPointAccessors
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleIconButton
import lab.p4c.nextup.app.ui.components.ThrottleOutlinedButton
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.feature.settings.ui.debug.SettingsDebugUiEvent
import lab.p4c.nextup.feature.settings.ui.debug.SettingsDebugViewModel
import lab.p4c.nextup.platform.permission.AccessibilityPermission
import lab.p4c.nextup.platform.permission.BatteryOptimizationPermission
import lab.p4c.nextup.platform.permission.ExactAlarmPermission
import lab.p4c.nextup.platform.permission.MicrophonePermission
import lab.p4c.nextup.platform.permission.NotificationPermission
import lab.p4c.nextup.platform.permission.OverlayPermission
import lab.p4c.nextup.platform.permission.SpeechSettingsIntents
import lab.p4c.nextup.platform.permission.UsageAccessPermission
import lab.p4c.nextup.platform.telemetry.permission.PermissionTelemetryEntryPoint

private const val ADMIN_MODE_PASSWORD = "wakeupnextup"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(navController: NavController) {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val settingsUi by settingsVm.ui.collectAsState()
    val debugVm: SettingsDebugViewModel = hiltViewModel()

    val ctx = LocalContext.current

    var exactGranted by remember { mutableStateOf(ExactAlarmPermission.canSchedule(ctx)) }
    var overlayGranted by remember { mutableStateOf(OverlayPermission.canDraw(ctx)) }
    var a11yEnabled by remember { mutableStateOf(AccessibilityPermission.isEnabled(ctx)) }
    var usageGranted by remember { mutableStateOf(UsageAccessPermission.isGranted(ctx)) }
    var notifGranted by remember { mutableStateOf(NotificationPermission.isGranted(ctx)) }
    var batteryIgnored by remember { mutableStateOf(BatteryOptimizationPermission.isIgnoring(ctx)) }
    var micGranted by remember { mutableStateOf(MicrophonePermission.isGranted(ctx)) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var isAdminMode by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }

    val app = ctx.applicationContext
    val tracker = remember {
        EntryPointAccessors.fromApplication(
            app,
            PermissionTelemetryEntryPoint::class.java
        ).permissionChangeTracker()
    }

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

                tracker.checkAndLog(source = "APP_FLOW")
                settingsVm.refreshExperimentInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }



    LaunchedEffect(Unit) {
        debugVm.events.collect { e ->
            when (e) {
                is SettingsDebugUiEvent.Toast ->
                    Toast.makeText(ctx, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    ThrottleIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    ThrottleIconButton(
                        onClick = {
                            adminPasswordInput = ""
                            showAdminDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "관리자 모드"
                        )
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("실험 정보 입력") },
                        supportingContent = {
                            Text(settingsUi.experimentInfoEntry.supportingText)
                        },
                        trailingContent = {
                            if (settingsUi.experimentInfoEntry.isCompleted) {
                                ThrottleOutlinedButton(
                                    onClick = { navController.navigate("experimentInfo") }
                                ) {
                                    Text(settingsUi.experimentInfoEntry.buttonText)
                                }
                            } else {
                                ThrottleButton(
                                    onClick = { navController.navigate("experimentInfo") }
                                ) {
                                    Text(settingsUi.experimentInfoEntry.buttonText)
                                }
                            }
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
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("차단할 앱 선택") },
                        supportingContent = { Text("알람 해제 후 오버레이로 차단할 앱을 선택하세요") },
                        trailingContent = {
                            ThrottleButton(
                                onClick = { navController.navigate("blockTargets") }
                            ) {
                                Text("설정")
                            }
                        }
                    )
                }
            }

            if (isAdminMode) {
                settingsDebugItems(
                    onNavigateUsage = { navController.navigate("usage") },
                    onClearTodaySurvey = { debugVm.clearTodaySurvey() },
                    onTestSurveyReminder = { debugVm.scheduleSurveyReminderInTenSec() },
                    onTestUploader = { debugVm.triggerUploaderInSeconds(ctx, 10) }
                )
            }
        }
    }

    if (showAdminDialog) {
        AdminModePasswordDialog(
            password = adminPasswordInput,
            onPasswordChange = { adminPasswordInput = it },
            onDismiss = { showAdminDialog = false },
            onConfirm = {
                if (adminPasswordInput == ADMIN_MODE_PASSWORD) {
                    isAdminMode = true
                    showAdminDialog = false
                    Toast.makeText(ctx, "관리자 모드 활성화", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

private fun LazyListScope.settingsDebugItems(
    onNavigateUsage: () -> Unit,
    onClearTodaySurvey: () -> Unit,
    onTestSurveyReminder: () -> Unit,
    onTestUploader: () -> Unit,
) {
    item {
        Text(
            text = "개발자",
            style = MaterialTheme.typography.titleSmall,
            color = NextUpThemeTokens.colors.textMuted
        )
    }

    item {
        val x = NextUpThemeTokens.colors
        val ctx = LocalContext.current

        Card(
            shape = MaterialTheme.shapes.medium,
        ) {
            ListItem(
                headlineContent = { Text("음성 인식 설정") },
                supportingContent = {
                    Text(
                        text = "음성 인식 및 오프라인 언어 설정을 확인합니다",
                        color = x.textSecondary
                    )
                },
                trailingContent = {
                    ThrottleButton(
                        onClick = { SpeechSettingsIntents.openSpeechRecognitionSettings(ctx) }
                    ) {
                        Text("열기")
                    }
                }
            )
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("사용량 통계 화면") },
                supportingContent = { Text("usage 화면으로 이동") },
                modifier = Modifier.clickableThrottle { onNavigateUsage() }
            )
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("오늘 설문 데이터 삭제") },
                supportingContent = { Text("Room에 저장된 오늘 설문 데이터를 제거") },
                trailingContent = {
                    ThrottleButton(onClick = onClearTodaySurvey) { Text("삭제") }
                }
            )
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("설문 리마인더 테스트") },
                supportingContent = { Text("10초 뒤 설문 리마인더 예약") },
                trailingContent = {
                    ThrottleButton(onClick = onTestSurveyReminder) { Text("예약") }
                }
            )
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("업로더 테스트") },
                supportingContent = { Text("10초 뒤 업로더 알람 트리거 (Android 12+)") },
                trailingContent = {
                    ThrottleButton(onClick = onTestUploader) { Text("실행") }
                }
            )
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
                    ThrottleButton(onClick = onClick) {
                        Text("설정")
                    }
                }
            }
        )
    }
}

@Composable
private fun AdminModePasswordDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("관리자 모드") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                singleLine = true,
                label = { Text("비밀번호") },
                visualTransformation = PasswordVisualTransformation(),
            )
        },
        confirmButton = {
            ThrottleButton(onClick = onConfirm) { Text("확인") }
        },
        dismissButton = {
            ThrottleOutlinedButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
