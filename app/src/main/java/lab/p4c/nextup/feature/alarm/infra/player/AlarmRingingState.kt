package lab.p4c.nextup.feature.alarm.infra.player

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRingingState @Inject constructor() {
    @Volatile private var ringing: Boolean = false

    fun setRinging(value: Boolean) {
        ringing = value
    }

    fun isRinging(): Boolean = ringing
}
