package lab.p4c.nextup.core.domain.alarm.model

import java.time.DayOfWeek

/**
 * Represents a single alarm configuration in the NextUp alarm system.
 *
 * This data class encapsulates all settings related to a user-defined alarm:
 * trigger time, repetition days, sound/vibration preferences, and snooze behavior.
 *
 * @property id Unique identifier for the alarm.
 * @property hour Hour of the alarm (0–23).
 * @property minute Minute of the alarm (0–59).
 * @property days Set of days of the week on which the alarm repeats. Empty = one-time alarm.
 * @property skipHolidays If true, alarm does not ring on public holidays.
 * @property enabled Whether the alarm is currently active.
 *
 * @property assetAudioPath Path to the alarm sound asset within the app bundle.
 * @property alarmSoundEnabled Whether the alarm sound is enabled.
 * @property ringtoneName Human-readable name of the selected ringtone.
 * @property volume Alarm volume (0.0–1.0).
 * @property fadeDuration Duration of fade-in effect in seconds. 0 = disabled.
 *
 * @property name Optional label displayed in the UI.
 * @property notificationBody Text used in system notification when alarm triggers.
 * @property loopAudio Whether to continuously loop the alarm sound until dismissed.
 * @property vibration Whether vibration is enabled during alarm playback.
 * @property warningNotificationOnKill If true, shows a warning notification when service is killed.
 * @property androidFullScreenIntent Whether to launch full-screen activity when alarm triggers.
 *
 * @property snoozeEnabled Enables snooze feature (re-triggering after interval).
 * @property snoozeInterval Minutes between snooze re-triggers.
 * @property maxSnoozeCount Maximum number of allowed snooze repeats.
 */
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