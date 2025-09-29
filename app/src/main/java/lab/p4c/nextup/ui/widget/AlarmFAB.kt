package lab.p4c.nextup.ui.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 네비게이션 트리거용 FAB.
 * Flutter의 onPressed → Add 화면 이동을 그대로 반영.
 */
@Composable
fun AlarmFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF9E9E9E), // Flutter Colors.grey 느낌
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "add")
    }
}

/**
 * 콜백형(필요 시): 새 알람 생성 콜백을 직접 받는 버전
 * 실제 저장은 Add 화면에서 수행하는 구조가 더 단순하므로 기본은 위 onClick 버전을 권장.
 */
@Composable
fun AlarmFAB(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") dummy: Unit = Unit // 시그니처 충돌 방지용
) {
    FloatingActionButton(
        onClick = onAdd,
        containerColor = Color(0xFF9E9E9E),
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "add")
    }
}
