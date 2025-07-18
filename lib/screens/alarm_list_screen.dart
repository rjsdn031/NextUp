import 'package:flutter/material.dart';
import 'dart:async';
import '../models/alarm_model.dart';
import '../utils/alarm_utils.dart';
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
                  days: alarm.days.map((d) => dayOfWeekToKor(d)).toList(),
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
