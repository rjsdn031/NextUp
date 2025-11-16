package lab.p4c.nextup.feature.usage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.usage.ui.model.UsageSession
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 라우트용: 패키지만 받고 공유 VM에서 세션을 가져와 아래 Screen에 전달 */
@Composable
fun UsageDetailRoute(
    appPackage: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedVm: UsageStatsSharedViewModel = hiltViewModel()
) {
    val sessions = sharedVm.getSessions(appPackage)
    UsageDetailScreen(
        appPackage = appPackage,
        sessions = sessions,
        onBack = onBack,
        modifier = modifier
    )
}

/** 프리젠테이션 컴포넌트(세션을 직접 받아 렌더) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageDetailScreen(
    appPackage: String,
    sessions: List<UsageSession>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val dateFmt = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.KOREA)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "$appPackage 사용 기록",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            if (sessions.isEmpty()) {
                Text(
                    text = "사용 기록이 없습니다.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sessions) { idx, s ->
                        val start = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(s.startMillis), zone
                        )
                        val end = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(s.endMillis), zone
                        )
                        val minutes = (s.durationMillis / 60_000L)

                        ListItem(
                            leadingContent = { Text("${idx + 1}") },
                            headlineContent = {
                                Text("${start.format(dateFmt)} ~ ${end.format(timeFmt)}")
                            },
                            supportingContent = {
                                Text("사용 시간: ${minutes}분")
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
