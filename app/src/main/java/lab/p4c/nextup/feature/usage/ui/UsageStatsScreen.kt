package lab.p4c.nextup.feature.usage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.feature.usage.ui.components.PermissionCard
import lab.p4c.nextup.feature.usage.ui.components.UsageRowItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageStatsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: UsageStatsViewModel = hiltViewModel()
) {
    val ui by vm.state.collectAsState()

    val c = MaterialTheme.colorScheme
    val x = NextUpThemeTokens.colors

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                vm.refreshOnResume()
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    Scaffold(
        containerColor = c.background,
        contentColor = c.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("앱별 총 사용 시간", color = c.onBackground) },
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
            when {
                !ui.hasPermission -> PermissionCard(
                    modifier = Modifier.align(Alignment.Center),
                    onOpenSettings = { vm.openUsageAccessSettings() }
                )

                ui.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = c.primary
                )

                ui.error != null -> Text(
                    text = ui.error!!,
                    color = c.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                ui.rows.isEmpty() -> Text(
                    text = "앱 사용 기록이 없습니다.",
                    color = x.textMuted,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.rows, key = { it.packageName }) { row ->
                        UsageRowItem(
                            row = row,
                            onClick = {
                                val startMs = ui.windowStartMs
                                val endMs = ui.windowEndMs
                                navController.navigate(
                                    "usage/detail/${row.packageName}?startMs=$startMs&endMs=$endMs"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
