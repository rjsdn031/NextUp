package lab.p4c.nextup.feature.alarm.infra.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.EntryPoints
import lab.p4c.nextup.feature.alarm.infra.player.AlarmPlayerService
import lab.p4c.nextup.feature.alarm.ui.ringing.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        if (id < 0) return

        val entryPoint = EntryPoints.get(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        val gate = entryPoint.blockGate()

        gate.rearmForNextAlarm()
        ContextCompat.startForegroundService(
            context,
            Intent(context, AlarmPlayerService::class.java)
                .putExtra(EXTRA_ALARM_ID, id)
        )

        context.startActivity(
            Intent(
                context,
                AlarmRingingActivity::class.java
            )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_ALARM_ID, id)
        )
    }

    companion object {
        const val ACTION_FIRE = "lab.p4c.nextup.action.ALARM_FIRE"
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}