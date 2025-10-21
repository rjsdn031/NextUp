// lab.p4c.nextup.util.SpeechSettingsIntents.kt
package lab.p4c.nextup.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

object SpeechSettingsIntents {

    /** 오프라인 음성 인식(한국어) 설치/관리 화면으로 최대한 가까운 경로로 보낸다. */
    fun openOfflineSpeechSettings(context: Context) {
        // 1) 음성 입력 설정 (가장 이상적)
        if (tryStart(context, Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))) return

        // 2) 언어 및 입력(IME) 설정로 폴백
        if (tryStart(context, Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))) return

        // 3) Google 앱 앱정보 화면 (여기서 "오프라인 음성 인식" 진입 가능)
        if (tryStart(
                context,
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:com.google.android.googlequicksearchbox".toUri()
                }
            )
        ) return

        // 4) 설정 메인으로 최종 폴백
        tryStart(context, Intent(Settings.ACTION_SETTINGS))
    }

    /** Google 앱(인식 엔진) 설정/업데이트가 필요할 때 */
    fun openGoogleAppDetails(context: Context) {
        tryStart(
            context,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:com.google.android.googlequicksearchbox".toUri()
            }
        )
    }

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}
