package lab.p4c.nextup.core.domain.alarm.model

/**
 * Domain model representing the source of an alarm sound.
 *
 * This type is platform-agnostic:
 * - Android [android.net.Uri] must not be referenced in core.
 * - URIs are represented as plain strings and are interpreted in infra layer.
 *
 * The infra layer is responsible for resolving a sound source into a playable resource
 * (e.g., raw resource id, content resolver stream).
 */
sealed class AlarmSound {

    /**
     * Sound bundled with the app (e.g., a file under `res/raw`).
     *
     * @property resName Resource entry name without extension (e.g., "alarm_soft_1").
     */
    data class Asset(val resName: String) : AlarmSound()

    /**
     * Sound provided by the Android system (alarm/ringtone/notification sounds).
     *
     * @property uri A string representation of a content URI.
     */
    data class System(val uri: String) : AlarmSound()

    /**
     * User-selected custom audio file.
     *
     * @property uri A string representation of a content URI that the app can read.
     * The infra layer may need persistable permissions for long-term access.
     */
    data class Custom(val uri: String) : AlarmSound()
}

/**
 * Returns a stable, non-localized label for logging or debugging.
 *
 * This is not a user-facing display name. UI should provide its own formatting
 * (e.g., resolving file display name via content resolver, mapping bundled assets to localized titles).
 * TODO(refactor): telemetry에서 사용하지 않는데, core에 있을 필요가 있는가?
 */
fun AlarmSound.toTitle(): String = when (this) {
    is AlarmSound.Asset -> resName
    is AlarmSound.System -> uri
    is AlarmSound.Custom -> uri
}
