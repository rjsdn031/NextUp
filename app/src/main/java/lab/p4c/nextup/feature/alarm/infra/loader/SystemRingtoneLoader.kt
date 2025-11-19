package lab.p4c.nextup.feature.alarm.infra.loader

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound

data class SystemTone(
    val title: String,
    val sound: AlarmSound.System
)

object SystemRingtoneLoader {

    fun loadSystemAlarms(context: Context): List<SystemTone> {
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_ALARM)
        }

        val cursor = manager.cursor
        val list = mutableListOf<SystemTone>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri: Uri = manager.getRingtoneUri(cursor.position)

            list.add(
                SystemTone(
                    title = title,
                    sound = AlarmSound.System(uri.toString())
                )
            )
        }

        return list
    }

    fun loadSystemNotifications(context: Context): List<SystemTone> {
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_NOTIFICATION)
        }

        val cursor = manager.cursor
        val list = mutableListOf<SystemTone>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri: Uri = manager.getRingtoneUri(cursor.position)

            list.add(
                SystemTone(
                    title = title,
                    sound = AlarmSound.System(uri.toString())
                )
            )
        }

        return list
    }

    fun loadSystemRingtones(context: Context): List<SystemTone> {
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_RINGTONE)
        }

        val cursor = manager.cursor
        val list = mutableListOf<SystemTone>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri: Uri = manager.getRingtoneUri(cursor.position)

            list.add(
                SystemTone(
                    title = title,
                    sound = AlarmSound.System(uri.toString())
                )
            )
        }

        return list
    }
}
