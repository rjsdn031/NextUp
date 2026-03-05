package lab.p4c.nextup.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleIconButton
import lab.p4c.nextup.app.ui.util.clickableThrottle
import lab.p4c.nextup.feature.settings.ui.util.UsageLabelFormatter

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
        onBack = onBack,
        onQueryChange = vm::onQueryChange,
        onClearQuery = vm::clearQuery,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockTargetSettingsScreen(
    ui: BlockTargetSettingsUi,
    onToggle: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    val t = MaterialTheme.typography
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("차단할 앱 선택", style = t.titleMedium) },
                navigationIcon = {
                    ThrottleIconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        },
        bottomBar = {
            ThrottleButton(
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

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            BlockTargetSearchBar(
                query = ui.query,
                onQueryChange = onQueryChange,
                onClear = {
                    onClearQuery()
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            if (ui.errorMessage != null) {
                Text(
                    text = ui.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp)
                )
            }

            if (ui.visibleItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ui.query.isBlank()) "표시할 앱이 없어요." else "검색 결과가 없어요.",
                        color = NextUpThemeTokens.colors.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.visibleItems) { item ->
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
    }
}

@Composable
private fun BlockTargetSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("앱 이름 또는 패키지명 검색") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "검색"
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                ThrottleIconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "검색어 지우기"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.primary,
            cursorColor = c.primary
        )
    )
}
@Composable
private fun BlockTargetItem(
    item: BlockTargetItemUi,
    onToggle: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val t = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableThrottle { onToggle() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = remember(item.packageName) {
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.appName,
                color = c.onBackground,
                style = t.bodyLarge
            )

            val desc = UsageLabelFormatter.forRollingDays(
                usageMillis = item.usageMillis,
                days = 7
            )

            Text(
                text = desc,
                color = NextUpThemeTokens.colors.textMuted,
                style = t.bodySmall
            )
        }

        Switch(
            checked = item.checked,
            onCheckedChange = { onToggle() }
        )
    }
}

