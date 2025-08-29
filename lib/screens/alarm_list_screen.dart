import 'package:flutter/material.dart';
import 'dart:async';
import '../models/alarm_model.dart';
import '../services/alarm_service.dart';
import '../services/navigation_service.dart';
import '../storage/alarm_storage.dart';
import '../widgets/alarm_list_view.dart';
import '../widgets/alarm_fab.dart';
import '../widgets/alarm_list_screen_app_bar.dart';
import 'add_alarm_screen.dart';
import 'alarm_ringing_screen.dart';

class AlarmListScreen extends StatefulWidget {
  const AlarmListScreen({super.key});

  @override
  State<AlarmListScreen> createState() => _AlarmListScreenState();
}

class _AlarmListScreenState extends State<AlarmListScreen> {
  final List<AlarmModel> alarms = [];
  DateTime now = DateTime.now();
  late final Timer _timer;

  @override
  void initState() {
    super.initState();
    _loadAlarms();
    _startClock();
  }

  void _startClock() {
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      setState(() => now = DateTime.now());
    });
  }

  Future<void> _loadAlarms() async {
    final loaded = await AlarmStorage.loadAlarms();
    setState(() {
      alarms
        ..clear()
        ..addAll(loaded);
      _sortAlarms();
    });
  }

  void _sortAlarms() {
    alarms.sort((a, b) {
      final aMinutes = a.time.hour * 60 + a.time.minute;
      final bMinutes = b.time.hour * 60 + b.time.minute;
      return aMinutes.compareTo(bMinutes);
    });
  }

  void _addAlarm(AlarmModel alarm) {
    setState(() => alarms.add(alarm));
    _sortAlarms();
    AlarmStorage.saveAlarms(alarms);

    if (alarm.enabled) {
      AlarmService.scheduleAlarm(alarm);
    }
  }

  void _updateAlarm(int index, AlarmModel alarm) {
    setState(() => alarms[index] = alarm);
    _sortAlarms();
    AlarmStorage.saveAlarms(alarms);

    if (alarm.enabled) {
      AlarmService.scheduleAlarm(alarm);
    } else {
      AlarmService.cancelAlarm(alarm.id);
    }
  }

  void _deleteAlarm(int index) {
    final deleted = alarms[index];
    setState(() => alarms.removeAt(index));
    AlarmStorage.saveAlarms(alarms);
    AlarmService.cancelAlarm(deleted.id);
  }

  void _toggleAlarm(int index, bool value) {
    setState(() => alarms[index].enabled = value);
    AlarmStorage.saveAlarms(alarms);

    if (value) {
      AlarmService.scheduleAlarm(alarms[index]);
    } else {
      AlarmService.cancelAlarm(alarms[index].id);
    }
  }

  Future<void> _onAlarmTap(
    BuildContext context,
    AlarmModel alarm,
    int index,
  ) async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => AddAlarmScreen(initialAlarm: alarm, index: index),
      ),
    );

    if (result is Map<String, dynamic>) {
      final int? resultIndex = result['index'] as int?;
      if (result['delete'] == true && resultIndex != null) {
        _deleteAlarm(resultIndex);
      } else if (result['alarm'] is AlarmModel && resultIndex != null) {
        _updateAlarm(resultIndex, result['alarm'] as AlarmModel);
      }
    } else if (result is AlarmModel) {
      _addAlarm(result);
    }
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: const AlarmListScreenAppBar(),
      body: AlarmListView(
        alarms: alarms,
        now: now,
        onDelete: _deleteAlarm,
        onUpdate: _updateAlarm,
        onAdd: _addAlarm,
        onToggle: _toggleAlarm,
        onTap: _onAlarmTap,
      ),
      floatingActionButton: AlarmFAB(onAdd: _addAlarm),
      //////////////////////////////////
      // floatingActionButton: FloatingActionButton(
      //   child: const Icon(Icons.bug_report),
      //   onPressed: () {
      //     NavigationService.push(
      //       MaterialPageRoute(
      //         builder: (_) => const AlarmRingingScreen(
      //           title: '더미 알람',
      //           body: '오버레이 테스트용 알람입니다.',
      //           alarmId: -999,
      //         ),
      //       ),
      //     );
      //   },
      // ),
      /////////////////////////////////
      // floatingActionButton: FloatingActionButton(
      //   onPressed: () {
      //     if (alarms.isNotEmpty) {
      //       Navigator.of(context).push(
      //         MaterialPageRoute(
      //           builder: (_) => AlarmRingingScreen(
      //             title: alarms[0].name.isNotEmpty ? alarms[0].name : '알람',
      //             body: alarms[0].notificationBody,
      //             alarmId: alarms[0].id,
      //           ),
      //         ),
      //       );
      //     } else {
      //       ScaffoldMessenger.of(context).showSnackBar(
      //         const SnackBar(content: Text('저장된 알람이 없습니다')),
      //       );
      //     }
      //   },
      //   child: const Icon(Icons.add),
      // ),
    );
  }
}
