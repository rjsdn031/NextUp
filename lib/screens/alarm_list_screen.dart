import 'package:flutter/material.dart';
import 'dart:async';
import '../models/alarm_model.dart';
import '../storage/alarm_storage.dart';
import '../widgets/alarm_list_view.dart';
import '../widgets/alarm_fab.dart';
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

  void _updateAlarm(int index, AlarmModel alarm) {
    setState(() => alarms[index] = alarm);
    _sortAlarms();
    AlarmStorage.saveAlarms(alarms);
  }

  void _deleteAlarm(int index) {
    setState(() => alarms.removeAt(index));
    AlarmStorage.saveAlarms(alarms);
  }

  void _addAlarm(AlarmModel alarm) {
    setState(() => alarms.add(alarm));
    _sortAlarms();
    AlarmStorage.saveAlarms(alarms);
  }

  void _toggleAlarm(int index, bool value) {
    setState(() => alarms[index].enabled = value);
    AlarmStorage.saveAlarms(alarms);
  }

  Future<void> _onAlarmTap(BuildContext context, AlarmModel alarm, int index) async {
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
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        toolbarHeight: 0,
      ),
      body: AlarmListView(
        alarms: alarms,
        now: now,
        onDelete: _deleteAlarm,
        onUpdate: _updateAlarm,
        onAdd: _addAlarm,
        onToggle: _toggleAlarm,
        onTap: _onAlarmTap,
      ),
      floatingActionButton: FloatingActionButton(onPressed: () {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => const AlarmRingingScreen(
              title: '테스트 알람',
              body: '전체화면 알람 UI 테스트 중입니다.',
            ),
          ),
        );
      },
        child: const Icon(Icons.add),),
      // floatingActionButton: AlarmFAB(onAdd: _addAlarm),
    );
  }
}
