import 'package:alarm/model/alarm_settings.dart';
import 'package:alarm/model/notification_settings.dart';
import 'package:alarm/model/volume_settings.dart';
import 'package:flutter/material.dart';

enum DayOfWeek {
  monday, tuesday, wednesday, thursday, friday, saturday, sunday
}

class AlarmModel {
  final int id;
  final TimeOfDay time;
  final List<DayOfWeek> days;
  final bool skipHolidays;
  bool enabled;

  final String assetAudioPath;
  final bool alarmSoundEnabled;
  final String ringtoneName;
  final double volume;
  final int fadeDuration;
  final String name;
  final String notificationBody;
  final bool loopAudio;
  final bool vibration;
  final bool warningNotificationOnKill;
  final bool androidFullScreenIntent;
  final bool snoozeEnabled;
  final int snoozeInterval;
  final int maxSnoozeCount;

  AlarmModel({
    required this.id,
    required this.time,
    required this.days,
    this.skipHolidays = true,
    this.enabled = true,
    this.name = '',
    this.assetAudioPath = 'assets/sounds/test_sound.mp3',
    this.alarmSoundEnabled = true,
    this.ringtoneName = 'Classic Bell',
    this.loopAudio = true,
    this.vibration = true,
    this.volume = 1.0,
    this.fadeDuration = 0,
    this.notificationBody = '기상 시간입니다.',
    this.androidFullScreenIntent = true,
    this.warningNotificationOnKill = true,
    this.snoozeEnabled = false,
    this.snoozeInterval = 5,
    this.maxSnoozeCount = 3,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'hour': time.hour,
    'minute': time.minute,
    'days': days.map((d) => d.index).toList(),
    'skipHolidays': skipHolidays,
    'assetAudioPath': assetAudioPath,
    'alarmSoundEnabled': alarmSoundEnabled,
    'ringtoneName': ringtoneName,
    'volume': volume,
    'fadeDuration': fadeDuration,
    'name': name,
    'notificationBody': notificationBody,
    'loopAudio': loopAudio,
    'vibration': vibration,
    'warningNotificationOnKill': warningNotificationOnKill,
    'androidFullScreenIntent': androidFullScreenIntent,
    'enabled': enabled,
    'snoozeEnabled': snoozeEnabled,
    'snoozeInterval': snoozeInterval,
    'maxSnoozeCount': maxSnoozeCount,
  };

  factory AlarmModel.fromJson(Map<String, dynamic> json) => AlarmModel(
    id: json['id'],
    time: TimeOfDay(hour: json['hour'], minute: json['minute']),
    days:
    (json['days'] as List).map((i) => DayOfWeek.values[i]).toList(),
    skipHolidays: json['skipHolidays'] ?? false,
    assetAudioPath: json['assetAudioPath'] ?? 'assets/sounds/test_sound.mp3',
    alarmSoundEnabled: json['alarmSoundEnabled'] ?? false,
    ringtoneName: json['ringtoneName'] ?? 'Classic Bell',
    volume: (json['volume'] ?? 1.0).toDouble(),
    fadeDuration: json['fadeDuration'] ?? 0,
    name: json['name'] ?? '',
    notificationBody: json['notificationBody'] ?? '기상 시간입니다.',
    loopAudio: json['loopAudio'] ?? true,
    vibration: json['vibration'] ?? true,
    warningNotificationOnKill: json['warningNotificationOnKill'] ?? true,
    androidFullScreenIntent: json['androidFullScreenIntent'] ?? true,
    enabled: json['enabled'] ?? true,
    snoozeEnabled: json['snoozeEnabled'] ?? false,
    snoozeInterval: json['snoozeInterval'] ?? 5,
    maxSnoozeCount: json['maxSnoozeCount'] ?? 3,
  );
}

extension AlarmModelExtension on AlarmModel {
  AlarmSettings toAlarmSettings(DateTime scheduledTime) {
    return AlarmSettings(
      id: id,
      dateTime: scheduledTime,
      assetAudioPath: alarmSoundEnabled ? assetAudioPath : '',
      volumeSettings: fadeDuration > 0
          ? VolumeSettings.fade(
        volume: volume,
        fadeDuration: Duration(seconds: fadeDuration),
      )
          : VolumeSettings.fixed(volume: volume),
      notificationSettings: NotificationSettings(
        title: name.isEmpty ? '알람' : name,
        body: notificationBody,
      ),
      loopAudio: loopAudio,
      vibrate: vibration,
      warningNotificationOnKill: warningNotificationOnKill,
      androidFullScreenIntent: androidFullScreenIntent,
    );
  }
}