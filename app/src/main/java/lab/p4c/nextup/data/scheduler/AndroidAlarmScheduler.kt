package lab.p4c.nextup.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.ui.screen.ringing.AlarmRingingActivity

@Singleton
class AndroidAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    override fun schedule(id: Int, triggerAtUtcMillis: Long, alarm: Alarm) {
        android.util.Log.d("Scheduler", "schedule id=$id at=$triggerAtUtcMillis")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        }

        val firePi = buildFirePendingIntent(id)
        setAlarmClock(id, triggerAtUtcMillis, firePi)
    }

    override fun cancel(id: Int) {
        // fire PI
        val fireIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
        }
        val firePi = PendingIntent.getBroadcast(
            context, id, fireIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (firePi != null) {
            alarmManager.cancel(firePi)
            firePi.cancel()
        }

        // show PI (AlarmClock용)
        val showIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            action = ACTION_SHOW_FROM_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        }
        val showPi = PendingIntent.getActivity(
            context, id, showIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        showPi?.cancel()
    }

    private fun setAlarmClock(
        id: Int,
        triggerAtUtcMillis: Long,
        firePi: PendingIntent
    ) {
        val showIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            action = ACTION_SHOW_FROM_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        }
        val showPi = PendingIntent.getActivity(
            context,
            id,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val info = AlarmManager.AlarmClockInfo(triggerAtUtcMillis, showPi)
        alarmManager.setAlarmClock(info, firePi)
    }

    private fun buildFirePendingIntent(id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        // 액션 네임으로 통일 (이전의 클래스 경로 문자열 대신)
        const val ACTION_SHOW_FROM_ALARM = "lab.p4c.nextup.action.SHOW_FROM_ALARM"
    }
}