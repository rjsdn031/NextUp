package lab.p4c.nextup.feature.alarm.infra.player

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound
import androidx.core.net.toUri

class AlarmPreviewPlayer(
    private val context: Context
) {
    private var player: MediaPlayer? = null

    suspend fun play(sound: AlarmSound) = withContext(Dispatchers.Main) {
        stop()

        player = MediaPlayer().apply {
            isLooping = false

            when (sound) {
                is AlarmSound.Asset -> {
                    // AlarmSound.Asset(resName = "test_sound")
                    val resId = context.resources.getIdentifier(
                        sound.resName,
                        "raw",
                        context.packageName
                    )

                    if (resId == 0)
                        throw IllegalArgumentException("Raw resource not found: ${sound.resName}")

                    val afd = context.resources.openRawResourceFd(resId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }

                is AlarmSound.System -> {
                    setDataSource(context, sound.uri.toUri())
                }

                is AlarmSound.Custom -> {
                    setDataSource(context, sound.uri.toUri())
                }
            }

            setOnPreparedListener { it.start() }
            prepareAsync()
        }
    }

    fun stop() {
        player?.stop()
        player?.reset()
        player?.release()
        player = null
    }
}
