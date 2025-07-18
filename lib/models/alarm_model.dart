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
}