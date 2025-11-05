package lab.p4c.nextup.feature.overlay.ui

enum class UnlockPhase {
    Idle,           // 초기: 버튼 대기
    Listening,      // 듣는 중
    Processing,     // 처리 중
    Matched,        // 충분히 인식됨
    Mismatch,       // 일치 안 함
    Timeout,        // 음성 없음/타임아웃
    Busy,           // 엔진 바쁨
    PermissionErr,  // 권한 문제
    ClientErr,      // 클라이언트 오류(기타)
}