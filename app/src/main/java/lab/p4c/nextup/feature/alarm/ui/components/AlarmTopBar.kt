package lab.p4c.nextup.feature.alarm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lab.p4c.nextup.app.ui.components.ThrottleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTopBar(
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    val c = MaterialTheme.colorScheme

    TopAppBar(
        title = {}, // no title
        navigationIcon = {
            ThrottleIconButton(onClick = { onMenuClick?.invoke() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "menu",
                    tint = c.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = c.background,
            scrolledContainerColor = c.surfaceVariant,
            navigationIconContentColor = c.onBackground,
            titleContentColor = c.onBackground
        ),
        modifier = modifier
    )
}
