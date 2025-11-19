package lab.p4c.nextup.feature.alarm.data.mapper

import lab.p4c.nextup.feature.alarm.data.local.entity.AlarmEntity
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.common.time.toDayOfWeekSet
import lab.p4c.nextup.core.common.time.toMask
import lab.p4c.nextup.core.domain.alarm.model.AlarmSound

fun AlarmEntity.toDomain(): Alarm {
    val sound = when (soundType) {
        "asset" -> AlarmSound.Asset(resName = soundValue)
        "system" -> AlarmSound.System(uri = soundValue)
        "custom" -> AlarmSound.Custom(uri = soundValue)
        else -> AlarmSound.Asset("test_sound")
    }

    return Alarm(
        id = id,
        hour = hour,
        minute = minute,
        days = repeatMask.toDayOfWeekSet(),
        skipHolidays = skipHolidays,
        enabled = enabled,

        sound = sound,
//        assetAudioPath = assetAudioPath,
        alarmSoundEnabled = alarmSoundEnabled,
//        ringtoneName = ringtoneName,
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
}

fun Alarm.toEntity(): AlarmEntity {
    val (type, value) = when (sound) {
        is AlarmSound.Asset -> "asset" to sound.resName
        is AlarmSound.System -> "system" to sound.uri
        is AlarmSound.Custom -> "custom" to sound.uri
    }

    return AlarmEntity(
        id = if (id == 0) 0 else id,
        hour = hour,
        minute = minute,
        repeatMask = days.toMask(),
        skipHolidays = skipHolidays,
        enabled = enabled,
        soundType = type,
        soundValue = value,
//        assetAudioPath = assetAudioPath,
        alarmSoundEnabled = alarmSoundEnabled,
//        ringtoneName = ringtoneName,
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
}
