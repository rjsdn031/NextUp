package lab.p4c.nextup.feature.overlay.ui

import androidx.compose.runtime.Composable

@Composable
fun phaseText(phase: UnlockPhase, errno: Int? = null): String = when (phase) {
    UnlockPhase.Idle         -> "버튼을 눌러 말하기를 시작하세요"
    UnlockPhase.Listening    -> "듣는 중…"
    UnlockPhase.Processing   -> "처리 중…"
    UnlockPhase.Matched      -> "‘이용하기’를 눌러 계속하세요"
    UnlockPhase.Mismatch     -> "일치하지 않아요. 다시 시도해 주세요."
    UnlockPhase.Timeout      -> "음성이 감지되지 않았어요"
    UnlockPhase.Busy         -> "음성 엔진 사용 중. 잠시 후 다시 시도"
    UnlockPhase.PermissionErr-> "권한 오류: 설정에서 마이크 허용 필요"
    UnlockPhase.ClientErr    -> "인식 오류가 발생했어요($errno)"
}