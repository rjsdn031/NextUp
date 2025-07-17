import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'package:timezone/timezone.dart' as tz;
import 'package:shared_preferences/shared_preferences.dart';

final notifications = FlutterLocalNotificationsPlugin();

Future<void> initNotifications() async {
  const androidInit = AndroidInitializationSettings('@mipmap/ic_launcher');

  const iosInit = DarwinInitializationSettings();

  await notifications.initialize(
    const InitializationSettings(android: androidInit, iOS: iosInit),
    onDidReceiveNotificationResponse: (resp) {
      // 알림 클릭 시 수행할 로직
    },
  );

  tz.initializeTimeZones(); // 타임존 데이터 로드
}

Future<void> requestIOSPermissions() async {
  await notifications
      .resolvePlatformSpecificImplementation<
        IOSFlutterLocalNotificationsPlugin
      >()
      ?.requestPermissions(alert: true, sound: true, badge: true);
}

/// id: 알람 식별자, dateTime: 알람 시간
/// 알람 예약
Future<void> scheduleAlarm(int id, DateTime dateTime) async {
  await notifications.zonedSchedule(
    id,
    'alarm',
    '일어나세요!',
    tz.TZDateTime.from(dateTime, tz.local),
    const NotificationDetails(
      android: AndroidNotificationDetails(
        'alarm_channel',
        'Alarm',
        importance: Importance.max,
        priority: Priority.high,
        playSound: false,
        // 알림만 띄우고
        fullScreenIntent: true,
        // 화면 켜기
        category: AndroidNotificationCategory.alarm,
      ),
      iOS: DarwinNotificationDetails(),
    ),
    androidAllowWhileIdle: true, // deprecated
    uiLocalNotificationDateInterpretation:
        UILocalNotificationDateInterpretation.absoluteTime,
    matchDateTimeComponents: DateTimeComponents.time, // 매일 반복하려면 사용
  );
}

/// 알람 취소
Future<void> cancelAlarm(int id) async => notifications.cancel(id);

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initNotifications();
  await requestIOSPermissions();
  runApp(const AlarmApp());
}

class AlarmApp extends StatefulWidget {
  const AlarmApp({super.key});

  @override
  State<AlarmApp> createState() => _AlarmAppState();
}

class _AlarmAppState extends State<AlarmApp> {
  final _prefs = SharedPreferences.getInstance();
  List<DateTime> alarms = [];

  @override
  void initState() {
    super.initState();
    _loadAlarms();
  }

  Future<void> _loadAlarms() async {
    final prefs = await _prefs;
    setState(() {
      alarms =
          (prefs.getStringList('alarms') ?? [])
              .map((e) => DateTime.parse(e))
              .toList();
    });
  }

  Future<void> _addAlarm(DateTime t) async {
    final prefs = await _prefs;
    final id = t.millisecondsSinceEpoch ~/ 1000; // 간단한 ID
    await scheduleAlarm(id, t);
    alarms.add(t);
    await prefs.setStringList(
      'alarms',
      alarms.map((e) => e.toIso8601String()).toList(),
    );
    setState(() {});
  }

  Future<void> _removeAlarm(DateTime date) async {
    final id = date.millisecondsSinceEpoch ~/ 1000;
    await cancelAlarm(id);
    alarms.remove(date);
    final prefs = await _prefs;
    await prefs.setStringList(
      'alarms',
      alarms.map((e) => e.toIso8601String()).toList(),
    );
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '알람 시계',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      home: Scaffold(
        appBar: AppBar(title: const Text('알람 시계')),
        body: ListView(
          children:
              alarms
                  .map(
                    (date) => ListTile(
                      leading: const Icon(Icons.alarm),
                      title: Text(TimeOfDay.fromDateTime(date).format(context)),
                      subtitle: Text(date.toLocal().toString()),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () => _removeAlarm(date),
                      ),
                    ),
                  )
                  .toList(),
        ),
        floatingActionButton: FloatingActionButton(
          child: const Icon(Icons.add),
          onPressed: () async {
            final picked = await showTimePicker(
              context: context,
              initialTime: TimeOfDay.now(),
            );
            if (picked != null) {
              final now = DateTime.now();
              final alarmTime = DateTime(
                now.year,
                now.month,
                now.day,
                picked.hour,
                picked.minute,
              );
              await _addAlarm(
                alarmTime.isBefore(now)
                    ? alarmTime.add(const Duration(days: 1))
                    : alarmTime,
              );
            }
          },
        ),
      ),
    );
  }
}
