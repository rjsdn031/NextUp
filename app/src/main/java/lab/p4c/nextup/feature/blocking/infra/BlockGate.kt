package lab.p4c.nextup.feature.blocking.infra

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockGate @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("nextup_prefs", Context.MODE_PRIVATE)
    }

    /** nowMillis 시점 기준으로 차단이 비활성화 되어있는지 */
    fun isDisabled(nowMillis: Long): Boolean {
        if (prefs.getBoolean(KEY_DISABLED_UNTIL_ALARM, false)) {
            return true
        }

        val readyUntil = prefs.getLong(KEY_BLOCK_READY_UNTIL, 0L)
        if (nowMillis < readyUntil) {
            return false
        } else {
            return true
        }
    }

    /** 다음 알람 전까지 전부 무력화 (문구 해제 성공 시) */
    fun disableUntilNextAlarm() {
        prefs.edit { putBoolean(KEY_DISABLED_UNTIL_ALARM, true) }
    }

    /** 알람 울릴 때(또는 알람 설정 변경 시) 다시 활성화 */
    fun rearmForNextAlarm() {
        prefs.edit { putBoolean(KEY_DISABLED_UNTIL_ALARM, false) }
    }

    /** n분 동안 block-ready 상태 */
    fun disableForMinutes(min: Long, nowMillis: Long) {
        val until = nowMillis + min * 60_000L
        prefs.edit { putLong(KEY_BLOCK_READY_UNTIL, until) }
    }

    /** 필요하면 ready-until 리셋 */
    fun clearReady() {
        prefs.edit { remove(KEY_BLOCK_READY_UNTIL) }
    }

    companion object {
        private const val KEY_DISABLED_UNTIL_ALARM = "blockDisabledUntilNextAlarm"
        private const val KEY_BLOCK_READY_UNTIL = "blockReadyUntil"
    }
}
