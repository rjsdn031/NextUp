package lab.p4c.nextup.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun BlockingOverlayView(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)) // 반투명 블랙
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDismiss() } // 더블탭으로 닫기
                )
            },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                text = "이 앱은 현재 차단되어 있습니다",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}
