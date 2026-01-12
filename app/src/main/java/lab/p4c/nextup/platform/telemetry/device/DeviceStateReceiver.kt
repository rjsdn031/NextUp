package lab.p4c.nextup.platform.telemetry.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.blocking.infra.BlockGate

@AndroidEntryPoint
class DeviceStateReceiver : BroadcastReceiver() {

    @Inject lateinit var telemetryLogger: TelemetryLogger
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var blockGate: BlockGate

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val now = timeProvider.now().toEpochMilli()

        // "Blocking 시간이 남아있는지 여부"
        val isBlocking = (!blockGate.isDisabled(now)).toString()

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                telemetryLogger.log(
                    eventName = "PhoneRebooted",
                    payload = mapOf("isBlocking" to isBlocking)
                )
            }

            Intent.ACTION_SHUTDOWN -> {
                // BestEffort
                telemetryLogger.log(
                    eventName = "Shutdown",
                    payload = mapOf("isBlocking" to isBlocking)
                )
            }

            Intent.ACTION_BATTERY_LOW -> {
                telemetryLogger.log(eventName = "BatteryLow", payload = emptyMap())
            }

            Intent.ACTION_BATTERY_OKAY -> {
                telemetryLogger.log(eventName = "BatteryOkay", payload = emptyMap())
            }
        }
    }
}
