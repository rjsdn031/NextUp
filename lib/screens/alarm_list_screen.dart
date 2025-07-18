import 'package:flutter/material.dart';
import 'dart:async';
import '../models/alarm_model.dart';
import '../widgets/alarm_tile.dart';

class AlarmListScreen extends StatefulWidget {
  const AlarmListScreen({super.key});

  @override
  State<AlarmListScreen> createState() => _AlarmListScreenState();
}

class _AlarmListScreenState extends State<AlarmListScreen> {
  // TODO: 동적 상태 리스트 사용하기
  final List<AlarmModel> alarms = [
    AlarmModel(
      time: TimeOfDay(hour: 6, minute: 0),
      days: [
        DayOfWeek.monday,
        DayOfWeek.tuesday,
        DayOfWeek.wednesday,
        DayOfWeek.thursday,
        DayOfWeek.friday,
      ],
      enabled: false,
    ),
    AlarmModel(
      time: TimeOfDay(hour: 8, minute: 0),
      days: [
        DayOfWeek.monday,
        DayOfWeek.tuesday,
        DayOfWeek.wednesday,
        DayOfWeek.thursday,
        DayOfWeek.friday,
      ],
    ),
    AlarmModel(
      time: TimeOfDay(hour: 9, minute: 0),
      days: [
        DayOfWeek.monday,
        DayOfWeek.tuesday,
        DayOfWeek.wednesday,
        DayOfWeek.thursday,
        DayOfWeek.friday,
      ],
    ),
  ];

  DateTime now = DateTime.now();
  late final Timer _timer;

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      setState(() {
        now = DateTime.now();
      });
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }

  void toggleAlarm(int index, bool value) {
    setState(() {
      alarms[index].enabled = value;
    });
  }

  // date formatting
  String formatDateTime(DateTime dt) {
    const weekDays = ['일', '월', '화', '수', '목', '금', '토'];
    final period = dt.hour < 12 ? '오전' : '오후';
    final hour = dt.hour % 12 == 0 ? 12 : dt.hour % 12;
    final minute = dt.minute.toString().padLeft(2, '0');
    final weekday = weekDays[dt.weekday % 7];
    return '${dt.month}월 ${dt.day}일 ($weekday) $period $hour:$minute';
  }

  // time formatting
  String formatTimeOfDay(TimeOfDay time) {
    final period = time.hour < 12 ? '오전' : '오후';
    final hour = time.hour % 12 == 0 ? 12 : time.hour % 12;
    final minute = time.minute.toString().padLeft(2, '0');
    return '$period $hour:$minute';
  }

  String _dayOfWeekToKor(DayOfWeek day) {
    switch (day) {
      case DayOfWeek.monday:
        return '월';
      case DayOfWeek.tuesday:
        return '화';
      case DayOfWeek.wednesday:
        return '수';
      case DayOfWeek.thursday:
        return '목';
      case DayOfWeek.friday:
        return '금';
      case DayOfWeek.saturday:
        return '토';
      case DayOfWeek.sunday:
        return '일';
    }
  }

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

    return '$hours시간 $minutes분 후\n알람이 울립니다';
  }

  @override
  Widget build(BuildContext context) {
    final height = MediaQuery.of(context).size.height;
    final now = DateTime.now();
    final nowFormatted = formatDateTime(now);

    // 가장 빠른 활성화된 알람
    final enabledAlarms = alarms.where((a) => a.enabled).toList();
    enabledAlarms.sort((a, b) {
      final dtA = DateTime(
        now.year,
        now.month,
        now.day,
        a.time.hour,
        a.time.minute,
      );
      final dtB = DateTime(
        now.year,
        now.month,
        now.day,
        b.time.hour,
        b.time.minute,
      );
      return dtA.isBefore(now)
          ? dtA.add(Duration(days: 1)).compareTo(dtB)
          : dtA.compareTo(dtB);
    });

    final nextAlarm = enabledAlarms.isNotEmpty ? enabledAlarms.first : null;
    final nextAlarmMessage = nextAlarm != null
        ? getTimeUntilAlarm(nextAlarm.time, now)
        : '설정된 다음 알람이 없습니다';

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        toolbarHeight: 0,
      ),

      body: Column(
        children: [
          SizedBox(
            height: height * 0.3,

            child: Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: 20.0,
                vertical: 32,
              ),

              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const Spacer(),
                  Text(
                    nextAlarmMessage,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 8),
                  Text(
                    nowFormatted,
                    style: const TextStyle(
                      color: Colors.grey,
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ),
          ),

          // 알람 리스트
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: alarms.length,
              itemBuilder: (context, index) {
                final alarm = alarms[index];

                return AlarmTile(
                  time: formatTimeOfDay(alarm.time),
                  days: alarm.days.map((d) => _dayOfWeekToKor(d)).toList(),
                  enabled: alarm.enabled,
                  onToggle: (val) => toggleAlarm(index, val),
                );
              },
            ),
          ),
        ],
      ),

      // FAB
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // TODO: 알람 추가 화면
        },
        backgroundColor: Colors.grey,
        child: const Icon(Icons.add, size: 28),
      ),
    );
  }
}
