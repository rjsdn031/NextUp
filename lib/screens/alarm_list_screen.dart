import 'package:flutter/material.dart';
import 'dart:async';
import '../models/alarm_model.dart';
import '../utils/alarm_utils.dart';
import '../widgets/alarm_tile.dart';
import 'add_alarm_screen.dart';

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

  void _sortAlarms() {
    alarms.sort((a, b) {
      final aMinutes = a.time.hour * 60 + a.time.minute;
      final bMinutes = b.time.hour * 60 + b.time.minute;
      return aMinutes.compareTo(bMinutes);
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

                return GestureDetector(
                  onTap: () async {
                    final result = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => AddAlarmScreen(initialAlarm: alarm, index: index),
                      ),
                    );

                    if (result is Map && result['alarm'] != null && result['index'] != null) {
                      setState(() {
                        alarms[result['index']] = result['alarm'];
                        _sortAlarms();
                      });
                    }

                    if (result is Map) {
                      final int? resultIndex = result['index'] as int?;

                      if (result['delete'] == true && resultIndex != null) {
                        setState(() {
                          alarms.removeAt(resultIndex);
                        });
                      } else if (result['alarm'] is AlarmModel && resultIndex != null) {
                        setState(() {
                          alarms[resultIndex] = result['alarm'];
                          _sortAlarms();
                        });
                      }
                    } else if (result is AlarmModel) {
                      setState(() {
                        alarms.add(result);
                        _sortAlarms();
                      });
                    }
                  },
                  child: AlarmTile(
                    time: formatTimeOfDay(alarm.time),
                    days: alarm.days.map((d) => dayOfWeekToKor(d)).toList(),
                    enabled: alarm.enabled,
                    onToggle: (val) => toggleAlarm(index, val),
                  ),
                );
              },
            ),
          ),
        ],
      ),

      // FAB
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          final newAlarm = await Navigator.push<AlarmModel>(
            context,
            MaterialPageRoute(
              builder: (_) => const AddAlarmScreen(),
            ),
          );

          if (newAlarm != null) {
            setState(() {
              alarms.add(newAlarm);
              _sortAlarms();
            });
          }
        },
        backgroundColor: Colors.grey,
        child: const Icon(Icons.add, size: 28),
      ),
    );
  }
}
