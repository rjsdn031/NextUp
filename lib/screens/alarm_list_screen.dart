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

  @override
  Widget build(BuildContext context) {
    final height = MediaQuery.of(context).size.height;
    final now = DateTime.now();
    final nowFormatted = formatDateTime(now);

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
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Spacer(),
                  const Text(
                    '14시간 21분 후\n알람이 울립니다', // TODO: 실제 알람 감지해서 동작하기
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    nowFormatted,
                    style: const TextStyle(color: Colors.grey, fontSize: 16),
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
