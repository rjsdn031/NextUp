// ui/screen/usage/UsageStatsScreen.kt
package lab.p4c.nextup.feature.usage.ui

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import lab.p4c.nextup.feature.usage.infra.UsageStatsService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageStatsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedVm: UsageStatsSharedViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(UsageStatsService.hasPermission(ctx)) }
    var isLoading by remember { mutableStateOf(false) }
    var rows by remember { mutableStateOf(listOf<AppUsageRow>()) }
    var sessionsByApp by remember { mutableStateOf(mapOf<String, List<UsageSession>>()) }
    var error by remember { mutableStateOf<String?>(null) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                val granted = UsageStatsService.hasPermission(ctx)
                hasPermission = granted
                if (granted) {
                    isLoading = true
                    scope.launch {
                        refreshViaService(ctx) { r, m, err ->
                            rows = r
                            sessionsByApp = m
                            error = err
                            isLoading = false
                            sharedVm.setSessions(m)                      // ✅ 세션맵 저장
                        }
                    }
                }
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            refreshViaService(ctx) { r, m, err ->
                rows = r
                sessionsByApp = m
                error = err
                isLoading = false
                sharedVm.setSessions(m)
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("앱별 총 사용 시간") }) }
    ) { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            when {
                !hasPermission -> PermissionCard(
                    modifier = Modifier.align(Alignment.Center),
                    onOpenSettings = { UsageStatsService.requestPermission(ctx) }
                )
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                rows.isEmpty() -> Text(
                    text = "앱 사용 기록이 없습니다.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows, key = { it.packageName }) { row ->
                        UsageRowItem(
                            row = row,
                            onClick = {
                                // ✅ 상세로 이동 (패키지명만 전달)
                                navController.navigate("usage/detail/${row.packageName}")
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ----- 아래 유틸/보조는 이전 답변 그대로 유지 ----- */
data class UsageSession(
    val startMillis: Long,
    val endMillis: Long,
    val packageName: String
) {
    val durationMillis: Long get() = (endMillis - startMillis).coerceAtLeast(0L)
}

data class AppUsageRow(
    val packageName: String,
    val totalMillis: Long
)


private fun queryUsageSessions(
    context: Context,
    startMillis: Long,
    endMillis: Long
): Pair<Map<String, List<UsageSession>>, Map<String, Long>> {
    val usm = context.getSystemService(UsageStatsManager::class.java)
    val events = usm.queryEvents(startMillis, endMillis)
    val sessionsByApp = mutableMapOf<String, MutableList<UsageSession>>()
    val lastFg = mutableMapOf<String, Long>()

    val tmp = UsageEvents.Event()
    while (events.hasNextEvent()) {
        events.getNextEvent(tmp)
        val pkg = tmp.packageName ?: continue

        val isResume =
            (tmp.eventType == UsageEvents.Event.ACTIVITY_RESUMED) ||
                    (tmp.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)

        val isPause =
            (tmp.eventType == UsageEvents.Event.ACTIVITY_PAUSED) ||
                    (tmp.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)

        when {
            isResume -> {
                // 연속 RESUMED/FOREGROUND 중복 방지
                val prev = lastFg[pkg]
                if (prev == null || tmp.timeStamp > prev) {
                    lastFg[pkg] = tmp.timeStamp
                }
            }
            isPause -> {
                val start = lastFg.remove(pkg)
                if (start != null && tmp.timeStamp >= start) {
                    sessionsByApp.getOrPut(pkg) { mutableListOf() }
                        .add(UsageSession(start, tmp.timeStamp, pkg))
                }
            }
            else -> {
                // ignore other event types
            }
        }
    }

    // 닫히지 않은 세션 보정: endMillis로 마감
    if (lastFg.isNotEmpty()) {
        lastFg.forEach { (pkg, start) ->
            sessionsByApp.getOrPut(pkg) { mutableListOf() }
                .add(UsageSession(start, endMillis, pkg))
        }
    }

    val totals = sessionsByApp.mapValues { (_, list) ->
        list.sumOf { it.durationMillis }
    }

    return sessionsByApp to totals
}
private suspend fun refreshViaService(
    context: Context,
    onResult: (
        rows: List<AppUsageRow>,
        sessionsByApp: Map<String, List<UsageSession>>,
        error: String?
    ) -> Unit
) {
    val result = UsageStatsService.fetch(context, range = Duration.ofHours(1))
    val uiRows = result.summary.map { row ->
        AppUsageRow(
            packageName = row.packageName,
            totalMillis = row.total.toMillis()
        )
    }
    val uiSessions = result.sessionsByApp.mapValues { (_, list) ->
        list.map { s ->
            UsageSession(
                startMillis = s.startMillis,
                endMillis = s.endMillis,
                packageName = s.packageName
            )
        }
    }
    onResult(uiRows, uiSessions, result.error)
}

@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit
) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("사용량 접근 권한 필요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("앱 사용 시간을 보기 위해 '사용 데이터 접근' 권한이 필요합니다. 설정에서 허용해 주세요.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenSettings) { Text("설정 열기") }
            }
        }
    }
}

@Composable
private fun UsageRowItem(
    row: AppUsageRow,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.packageName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "총 사용 시간: ${formatDuration(row.totalMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSec = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = totalSec / 60
    val seconds = (totalSec % 60)
    return "${minutes}분 ${seconds}초"
}
