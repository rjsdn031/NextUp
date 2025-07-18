import 'package:flutter/material.dart';

enum DayOfWeek {
  monday, tuesday, wednesday, thursday, friday, saturday, sunday
}

class AlarmModel {
  final TimeOfDay time;
  final List<DayOfWeek> days;
  bool enabled;

  AlarmModel({
    required this.time,
    required this.days,
    this.enabled = true,
  });
}