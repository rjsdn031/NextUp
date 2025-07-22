import 'package:flutter/material.dart';

enum DayOfWeek {
  monday, tuesday, wednesday, thursday, friday, saturday, sunday
}

class AlarmModel {
  final TimeOfDay time;
  final List<DayOfWeek> days;
  bool enabled;

  final String name;
  final bool skipHolidays;
  final bool vibration;
  final bool snoozeEnabled;
  final String ringtone;

  AlarmModel({
    required this.time,
    required this.days,
    this.enabled = true,

    this.name = '',
    this.skipHolidays = false,
    this.vibration = true,
    this.snoozeEnabled = true,
    this.ringtone = 'null',
  });

  // 알람 저장소 저장
  Map<String, dynamic> toJson() => {
    'hour': time.hour,
    'minute': time.minute,
    'days': days.map((d) => d.index).toList(),
    'name': name,
    'skipHolidays': skipHolidays,
    'vibration': vibration,
    'snoozeEnabled': snoozeEnabled,
    'ringtone': ringtone,
    'enabled': enabled,
  };

  factory AlarmModel.fromJson(Map<String, dynamic> json) => AlarmModel(
    time: TimeOfDay(hour: json['hour'], minute: json['minute']),
    days: (json['days'] as List).map((i) => DayOfWeek.values[i]).toList(),
    name: json['name'] ?? '',
    skipHolidays: json['skipHolidays'] ?? false,
    vibration: json['vibration'] ?? true,
    snoozeEnabled: json['snoozeEnabled'] ?? true,
    ringtone: json['ringtone'] ?? 'Classic Bell',
    enabled: json['enabled'] ?? true,
  );

}