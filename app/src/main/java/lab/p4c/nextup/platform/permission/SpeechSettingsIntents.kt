package lab.p4c.nextup.platform.permission

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Opens speech-related system settings with safe fallbacks.
 *
 * This helper does not guarantee navigation to a dedicated offline speech pack screen,
 * because Android does not provide a stable public intent for that destination across devices.
 * Instead, it routes the user to the closest available speech/input settings screen.
 */
object SpeechSettingsIntents {

    private const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val GOOGLE_SPEECH_SERVICES_PACKAGE = "com.google.android.tts"

    /**
     * Opens the closest available speech recognition settings screen.
     *
     * Fallback order:
     * 1. Voice input settings
     * 2. Input method settings
     * 3. Speech Services by Google app details
     * 4. Google app details
     * 5. General settings
     *
     * @param context Context used to launch the settings activity.
     */
    fun openSpeechRecognitionSettings(context: Context) {
        val candidates = listOf(
//            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS),
            buildAppDetailsIntent(GOOGLE_SPEECH_SERVICES_PACKAGE),
            buildAppDetailsIntent(GOOGLE_APP_PACKAGE),
            Intent(Settings.ACTION_SETTINGS)
        )

        candidates.firstOrNull { canHandle(context, it) }
            ?.let { start(context, it) }
    }

    private fun buildAppDetailsIntent(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
        }

    private fun canHandle(context: Context, intent: Intent): Boolean {
        val packageManager: PackageManager = context.packageManager
        return intent.resolveActivity(packageManager) != null
    }

    private fun start(context: Context, intent: Intent) {
        try {
            context.startActivity(
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            // No-op.
        } catch (_: SecurityException) {
            // No-op.
        }
    }
}