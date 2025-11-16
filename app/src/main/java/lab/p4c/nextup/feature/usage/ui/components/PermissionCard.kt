package lab.p4c.nextup.feature.usage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionCard(
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