import 'package:alarm/alarm.dart';
import 'package:alarm/utils/alarm_set.dart';
import 'package:flutter/material.dart';
import 'package:nextup/screens/usage_stats_screen.dart';
import 'dart:async';
import '../main.dart';
import '../models/alarm_model.dart';
import '../services/alarm_service.dart';
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
  static StreamSubscription<AlarmSet>? _ringSubscription;

  @override
  void initState() {
    super.initState();
    _loadAlarms();

    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      setState(() => now = DateTime.now());
    });

    _ringSubscription ??= Alarm.ringing.listen((alarmSet) {
      if (alarmSet.alarms.isNotEmpty) {
        final alarm = alarmSet.alarms.first;

        navigatorKey.currentState?.push(
          MaterialPageRoute(
            builder: (_) => AlarmRingingScreen(
              title: alarm.notificationSettings?.title ?? '알람',
              body: alarm.notificationSettings?.body ?? '일어날 시간입니다!',
              alarmId: alarm.id,
            ),
          ),
        );
      }
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
    _ringSubscription?.cancel();
    _ringSubscription = null;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        toolbarHeight: kToolbarHeight,
        leading: IconButton(
          icon: const Icon(Icons.menu, color: Colors.white),
          onPressed: () {
            Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const UsageStatsScreen()),
            );
          },
        ),
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
      floatingActionButton: AlarmFAB(onAdd: _addAlarm),
    );
  }
}
