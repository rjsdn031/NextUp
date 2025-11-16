package lab.p4c.nextup.feature.usage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.feature.usage.ui.model.UsageSession
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageDetailScreen(
    appPackage: String,
    sessions: List<UsageSession>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors

    val zone = ZoneId.systemDefault()
    val dateFmt = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.KOREA)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

    Scaffold(
        containerColor = c.background,
        contentColor = c.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "$appPackage 사용 기록",
                        color = c.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = c.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.background,
                    titleContentColor = c.onBackground,
                    navigationIconContentColor = c.onBackground
                )
            )
        }
    ) { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(c.background)
                .padding(inner)
        ) {
            if (sessions.isEmpty()) {
                Text(
                    text = "사용 기록이 없습니다.",
                    color = x.textMuted,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
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
                            headlineContent = {
                                Text(
                                    "${start.format(dateFmt)} ~ ${end.format(timeFmt)}",
                                    color = c.onSurface
                                )
                            },
                            supportingContent = {
                                Text(
                                    "사용 시간: ${minutes}분",
                                    color = x.textSecondary
                                )
                            },
                            leadingContent = {
                                Text(
                                    "${idx + 1}",
                                    color = c.onSurface
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = c.surface,
                                headlineColor = c.onSurface,
                                supportingColor = x.textSecondary
                            )
                        )

                        HorizontalDivider(
                            color = c.outline.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
