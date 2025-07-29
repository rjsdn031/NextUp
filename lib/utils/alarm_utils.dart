import 'package:flutter/material.dart';
import '../models/alarm_model.dart';

// Date formatting
String formatDateTime(DateTime dt) {
  const weekDays = ['일', '월', '화', '수', '목', '금', '토'];
  final period = dt.hour < 12 ? '오전' : '오후';
  final hour = dt.hour % 12 == 0 ? 12 : dt.hour % 12;
  final minute = dt.minute.toString().padLeft(2, '0');
  final weekday = weekDays[dt.weekday % 7];
  return '${dt.month}월 ${dt.day}일 ($weekday) $period $hour:$minute';
}

// Time formatting
String formatTimeOfDay(TimeOfDay time) {
  final period = time.hour < 12 ? '오전' : '오후';
  final hour = time.hour % 12 == 0 ? 12 : time.hour % 12;
  final minute = time.minute.toString().padLeft(2, '0');
  return '$period $hour:$minute';
}

// Convert dayOfWeek to String
String dayOfWeekToKor(DayOfWeek day) {
  switch (day) {
    case DayOfWeek.monday: return '월';
    case DayOfWeek.tuesday: return '화';
    case DayOfWeek.wednesday: return '수';
    case DayOfWeek.thursday: return '목';
    case DayOfWeek.friday: return '금';
    case DayOfWeek.saturday: return '토';
    case DayOfWeek.sunday: return '일';
  }
}

// Detect nearest alarm time
String getTimeUntilAlarm(TimeOfDay alarmTime, DateTime now) {
  final todayAlarm = DateTime(
    now.year,
    now.month,
    now.day,
    alarmTime.hour,
    alarmTime.minute,
  );

  final alarmDateTime = todayAlarm.isBefore(now)
      ? todayAlarm.add(const Duration(days: 1))
      : todayAlarm;

  final difference = alarmDateTime.difference(now);
  final hours = difference.inHours;
  final minutes = difference.inMinutes % 60;

  String msg = '';
  if (difference >= Duration(minutes: 1)) {
    msg = '$hours시간 $minutes분 후\n알람이 울립니다';
  } else {
    msg = '잠시 후\n알람이 울립니다';
  }

  return msg;
}