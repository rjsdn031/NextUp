import 'package:alarm/alarm.dart';
import '../models/alarm_model.dart';

class AlarmService {
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
}
