package lab.p4c.nextup.domain.model

import java.time.DayOfWeek

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: Set<DayOfWeek> = emptySet(),
    val skipHolidays: Boolean = true,
    val enabled: Boolean = true,

    val assetAudioPath: String = "assets/sounds/test_sound.mp3",
    val alarmSoundEnabled: Boolean = true,
    val ringtoneName: String = "Classic Bell",
    val volume: Double = 1.0,
    val fadeDuration: Int = 0,
    val name: String = "",
    val notificationBody: String = "기상 시간입니다.",
    val loopAudio: Boolean = true,
    val vibration: Boolean = true,
    val warningNotificationOnKill: Boolean = true,
    val androidFullScreenIntent: Boolean = true,
    val snoozeEnabled: Boolean = false,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = 3
)