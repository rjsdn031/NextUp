package lab.p4c.nextup.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.p4c.nextup.app.ui.theme.NextUpThemeTokens
import lab.p4c.nextup.feature.settings.ui.model.BlockTargetItemUi
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun BlockTargetSettingsRoute(
    onBack: () -> Unit,
    vm: BlockTargetSettingsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    BlockTargetSettingsScreen(
        ui = ui,
        onToggle = vm::toggle,
        onSave = vm::save,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockTargetSettingsScreen(
    ui: BlockTargetSettingsUi,
    onToggle: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val t = MaterialTheme.typography
    val c = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("차단할 앱 선택", style = t.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onSave()
                    onBack()
                },
                enabled = ui.hasChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("저장하기")
            }
        }
    ) { innerPadding ->

        if (ui.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(ui.items) { item ->
                BlockTargetItem(
                    item = item,
                    onToggle = { onToggle(item.packageName) }
                )
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = NextUpThemeTokens.colors.divider
                )
            }
        }
    }
}

@Composable
private fun BlockTargetItem(
    item: BlockTargetItemUi,
    onToggle: () -> Unit
) {
    val c = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = remember(item.icon) {
            item.icon?.toBitmap()
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = item.appName,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(c.surfaceVariant, shape = MaterialTheme.shapes.small)
            )
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = item.appName,
            modifier = Modifier.weight(1f),
            color = c.onBackground
        )

        Switch(
            checked = item.checked,
            onCheckedChange = { onToggle() }
        )
    }
}
