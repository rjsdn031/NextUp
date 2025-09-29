package lab.p4c.nextup.ui.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTopBar(
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null // 전달 안 하면 no-op
) {
    TopAppBar(
        title = {}, // 타이틀 없음 (레거시 동일)
        navigationIcon = {
            IconButton(onClick = { onMenuClick?.invoke() /* null이면 아무 동작 없음 */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "menu",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            navigationIconContentColor = Color.White
        ),
        modifier = modifier
    )
}
