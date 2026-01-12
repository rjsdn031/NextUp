package lab.p4c.nextup.platform.telemetry.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger

class ChargingTelemetryReceiver(
    private val telemetryLogger: TelemetryLogger,
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                val (plugType, batteryStatus) = readChargingState(context)

                telemetryLogger.log(
                    eventName = "ChargingChanged",
                    payload = mapOf(
                        "batteryStatus" to batteryStatus,
                        "plugType" to plugType
                    )
                )
            }
        }
    }

    private fun readChargingState(context: Context): Pair<String, String> {
        val i = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return Pair( "UNKNOWN", "UNKNOWN")

        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"
            BatteryManager.BATTERY_STATUS_FULL -> "FULL"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT_CHARGING"
            else -> "UNKNOWN"
        }

        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
            0 -> "NONE"
            else -> "UNKNOWN"
        }

        return Pair(statusStr, plugType)
    }
}