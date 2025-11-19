package lab.p4c.nextup.feature.alarm.ui.picker

import lab.p4c.nextup.core.domain.alarm.model.AlarmSound

data class LocalSoundItem(
    val title: String,
    val sound: AlarmSound
)

// 앱에 넣어둔 raw 리소스 기반 Sound 목록 Todo: 내장 sound 재확인 필요
val defaultAppSounds = listOf(
    LocalSoundItem("기본 알람", AlarmSound.Asset("test_sound")),
//    LocalSoundItem("은은한 차임", AlarmSound.Asset("soft_chime")),
//    LocalSoundItem("경쾌한 알람", AlarmSound.Asset("happy_alarm")),
)
