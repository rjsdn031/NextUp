package lab.p4c.nextup.core.domain.alarm.model

sealed class AlarmSound {

    /**
     * 앱 번들에 포함된 raw 리소스 기반 알람음
     * 예: raw/alarm_soft_1
     */
    data class Asset(val resName: String) : AlarmSound()

    /**
     * Android 시스템 기본 제공 알람/벨소리
     * 예: content://media/...
     */
    data class System(val uri: String) : AlarmSound()

    /**
     * 사용자가 파일 선택기로 불러온 커스텀 음원
     */
    data class Custom(val uri: String) : AlarmSound()
}

fun AlarmSound.toTitle(): String = when (this) {
    is AlarmSound.Asset -> resName
    is AlarmSound.System -> uri
    is AlarmSound.Custom -> uri
}
