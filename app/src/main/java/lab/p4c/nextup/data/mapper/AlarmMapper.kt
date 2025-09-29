package lab.p4c.nextup.data.mapper

import lab.p4c.nextup.data.local.entity.AlarmEntity
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.util.toDayOfWeekSet
import lab.p4c.nextup.util.toMask

fun AlarmEntity.toDomain(): Alarm =
    Alarm(
        id = id,
        hour = hour,
        minute = minute,
        days = repeatMask.toDayOfWeekSet(),
        skipHolidays = skipHolidays,
        enabled = enabled,
        assetAudioPath = assetAudioPath,
        alarmSoundEnabled = alarmSoundEnabled,
        ringtoneName = ringtoneName,
        volume = volume,
        fadeDuration = fadeDuration,
        name = name,
        notificationBody = notificationBody,
        loopAudio = loopAudio,
        vibration = vibration,
        warningNotificationOnKill = warningNotificationOnKill,
        androidFullScreenIntent = androidFullScreenIntent,
        snoozeEnabled = snoozeEnabled,
        snoozeInterval = snoozeInterval,
        maxSnoozeCount = maxSnoozeCount
    )

fun Alarm.toEntity(): AlarmEntity =
    AlarmEntity(
        id = if (id == 0) 0 else id,
        hour = hour,
        minute = minute,
        repeatMask = days.toMask(),
        skipHolidays = skipHolidays,
        enabled = enabled,
        assetAudioPath = assetAudioPath,
        alarmSoundEnabled = alarmSoundEnabled,
        ringtoneName = ringtoneName,
        volume = volume,
        fadeDuration = fadeDuration,
        name = name,
        notificationBody = notificationBody,
        loopAudio = loopAudio,
        vibration = vibration,
        warningNotificationOnKill = warningNotificationOnKill,
        androidFullScreenIntent = androidFullScreenIntent,
        snoozeEnabled = snoozeEnabled,
        snoozeInterval = snoozeInterval,
        maxSnoozeCount = maxSnoozeCount
    )
