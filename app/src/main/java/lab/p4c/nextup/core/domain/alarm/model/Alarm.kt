package lab.p4c.nextup.core.domain.alarm.model

import java.time.DayOfWeek

/**
 * Domain model representing a single alarm configuration.
 *
 * This class contains only user-defined configuration and does not include
 * runtime state (e.g., current snooze count or trigger status).
 *
 * Alarm type is determined by [days]:
 * - If empty, the alarm is one-time.
 * - If non-empty, the alarm repeats on the specified days.
 *
 * This model is platform-agnostic and must not depend on Android APIs.
 *
 * Invariants:
 * - [hour] must be in 0..23.
 * - [minute] must be in 0..59.
 * - [volume] should be in 0.0..1.0.
 * - [snoozeInterval] must be positive when [snoozeEnabled] is true.
 *
 * @property id Unique identifier of the alarm.
 * @property hour Hour of day in 24-hour format (0–23).
 * @property minute Minute of hour (0–59).
 * @property days Days of week for repetition. Empty set indicates one-time alarm.
 * @property skipHolidays If true, the alarm will not trigger on public holidays.
 * @property enabled Whether the alarm is active.
 *
 * @property sound Sound configuration of the alarm.
 * @property alarmSoundEnabled Whether audio playback is enabled.
 * @property volume Alarm playback volume (0.0–1.0).
 * @property fadeDuration Fade-in duration in seconds. 0 disables fade-in.
 *
 * @property name Optional user-visible label.
 * @property notificationBody Text displayed in system notification when triggered.
 * @property loopAudio Whether playback should loop until dismissed.
 * @property vibration Whether vibration should be enabled during playback.
 *
 * @property warningNotificationOnKill Whether to show warning notification if service is killed.
 * @property androidFullScreenIntent Whether to launch full-screen UI on trigger.
 *
 * @property snoozeEnabled Whether snooze functionality is enabled.
 * @property snoozeInterval Interval in minutes between snooze triggers.
 * @property maxSnoozeCount Maximum number of snooze repetitions allowed.
 */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: Set<DayOfWeek> = emptySet(),
    val skipHolidays: Boolean = true,
    val enabled: Boolean = true,

    val sound: AlarmSound = AlarmSound.Asset(resName = "test_sound"),
    val alarmSoundEnabled: Boolean = true,
    val volume: Double = 1.0,
    val fadeDuration: Int = 0,

    val name: String = "",
    val notificationBody: String = "",
    val loopAudio: Boolean = true,
    val vibration: Boolean = true,

    val warningNotificationOnKill: Boolean = true,  // TODO(refactor):도메인에 있어야 하나?
    val androidFullScreenIntent: Boolean = true, // TODO(refactor):도메인에 있어야 하나?

    val snoozeEnabled: Boolean = false,
    val snoozeInterval: Int = 5,
    val maxSnoozeCount: Int = 3
)
