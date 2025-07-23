import 'package:flutter/material.dart';
import '../models/alarm_model.dart';
import '../utils/alarm_utils.dart';
import '../widgets/alarm_tile.dart';
import 'alarm_header.dart';

class AlarmListView extends StatelessWidget {
  final List<AlarmModel> alarms;
  final DateTime now;
  final void Function(int) onDelete;
  final void Function(int, AlarmModel) onUpdate;
  final void Function(AlarmModel) onAdd;
  final void Function(int, bool) onToggle;
  final void Function(BuildContext, AlarmModel, int) onTap;

  const AlarmListView({
    super.key,
    required this.alarms,
    required this.now,
    required this.onDelete,
    required this.onUpdate,
    required this.onAdd,
    required this.onToggle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final enabledAlarms = alarms.where((a) => a.enabled).toList();
    enabledAlarms.sort((a, b) => _alarmDateTime(a.time).compareTo(_alarmDateTime(b.time)));

    final nextAlarm = enabledAlarms.isNotEmpty ? enabledAlarms.first : null;
    final nextAlarmMessage = nextAlarm != null
        ? getTimeUntilAlarm(nextAlarm.time, now)
        : '설정된 다음 알람이 없습니다';

    return Column(
      children: [
        AlarmHeader(
          now: now,
          nextAlarmMessage: nextAlarmMessage,
        ),
        Expanded(
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            itemCount: alarms.length,
            itemBuilder: (context, index) {
              final alarm = alarms[index];
              return GestureDetector(
                onTap: () => onTap(context, alarm, index),
                child: AlarmTile(
                  time: formatTimeOfDay(alarm.time),
                  days: alarm.days.map(dayOfWeekToKor).toList(),
                  enabled: alarm.enabled,
                  onToggle: (val) => onToggle(index, val),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  DateTime _alarmDateTime(TimeOfDay time) {
    final scheduled = DateTime(now.year, now.month, now.day, time.hour, time.minute);
    return scheduled.isBefore(now) ? scheduled.add(const Duration(days: 1)) : scheduled;
  }
}