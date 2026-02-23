package lab.p4c.nextup.feature.alarm.infra.loader

import android.content.Context
import android.media.RingtoneManager
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import lab.p4c.nextup.feature.alarm.ui.picker.defaultAppSounds
import lab.p4c.nextup.feature.alarm.ui.picker.UriNameResolver

/**
 * Resolves a user-facing title for an [AlarmSound].
 *
 * - [AlarmSound.Asset] is resolved from app-defined sound list.
 * - [AlarmSound.System] is resolved via [RingtoneManager].
 * - [AlarmSound.Custom] is resolved via [UriNameResolver] first, then [RingtoneManager] as fallback.
 */
class AlarmSoundTitleResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun resolve(sound: AlarmSound): String {
        return when (sound) {
            is AlarmSound.Asset -> {
                defaultAppSounds.firstOrNull { it.sound == sound }?.title
                    ?: sound.resName
            }

            is AlarmSound.System -> {
                ringtoneTitle(sound.uri) ?: sound.uri
            }

            is AlarmSound.Custom -> {
                UriNameResolver.displayName(context, sound.uri.toUri())
                    ?: ringtoneTitle(sound.uri)
                    ?: sound.uri
            }
        }
    }

    private fun ringtoneTitle(uri: String): String? {
        return runCatching {
            RingtoneManager.getRingtone(context, uri.toUri())?.getTitle(context)
        }.getOrNull()
    }
}