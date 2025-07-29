import 'dart:async';
import 'package:alarm/alarm.dart';
import 'package:alarm/utils/alarm_set.dart';
import 'package:flutter/material.dart';
import '../models/alarm_model.dart';
import '../screens/alarm_ringing_screen.dart';
import 'navigation_service.dart';

class AlarmService {
  static StreamSubscription<AlarmSet>? _subscription;

  static Future<void> init() async {
    await Alarm.init();
  }

  static Future<void> scheduleAlarm(AlarmModel model) async {
    final now = DateTime.now();
    DateTime scheduled = DateTime(
      now.year,
      now.month,
      now.day,
      model.time.hour,
      model.time.minute,
    );
    if (scheduled.isBefore(now)) {
      scheduled = scheduled.add(const Duration(days: 1));
    }

    final alarmSettings = AlarmSettings(
      id: model.id,
      dateTime: scheduled,
      assetAudioPath: model.assetAudioPath,
      volumeSettings: VolumeSettings.fixed( // fixed volume, fade시 .fade()
        volume: model.volume,
      ),
      notificationSettings: NotificationSettings(
        title: model.name.isEmpty ? '알람' : model.name,
        body: model.notificationBody,
      ),
      loopAudio: model.loopAudio,
      vibrate: model.vibration,
      warningNotificationOnKill: model.warningNotificationOnKill,
      androidFullScreenIntent: model.androidFullScreenIntent,
    );

    await Alarm.set(alarmSettings: alarmSettings);
  }

  static Future<void> cancelAlarm(int id) async {
    await Alarm.stop(id);
  }

  static Future<void> cancelAllAlarms() async {
    final alarms = await Alarm.getAlarms();
    for (final alarm in alarms) {
      await Alarm.stop(alarm.id);
    }
  }

  static void startListening() {
    if (_subscription != null) return;

    _subscription = Alarm.ringing.listen((alarmSet) {
      if (alarmSet.alarms.isEmpty) return;
      final alarm = alarmSet.alarms.first;

      NavigationService.push(
        MaterialPageRoute(
          builder: (_) => AlarmRingingScreen(
            title: alarm.notificationSettings?.title ?? '알람',
            body: alarm.notificationSettings?.body ?? '일어날 시간입니다!',
            alarmId: alarm.id,
          ),
        ),
      );
    });
  }

  static void stopListening() {
    _subscription?.cancel();
    _subscription = null;
  }
}
