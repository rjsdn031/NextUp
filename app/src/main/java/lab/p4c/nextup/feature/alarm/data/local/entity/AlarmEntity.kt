package lab.p4c.nextup.feature.alarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    /** MON=bit0 .. SUN=bit6 (도메인 Set<DayOfWeek> ↔ 매퍼에서 변환) */
    val repeatMask: Int = 0,
    val skipHolidays: Boolean = true,
    val enabled: Boolean = true,

    val soundType: String, // "asset" | "system" | "custom"
    val soundValue: String, // resName OR uri

//    val assetAudioPath: String = "assets/sounds/test_sound.mp3",
    val alarmSoundEnabled: Boolean = true,
//    val ringtoneName: String = "Classic Bell",
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
