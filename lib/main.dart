import 'package:alarm/alarm.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:nextup/screens/alarm_ringing_screen.dart';
import 'package:nextup/screens/usage_stats_screen.dart';
import 'package:nextup/services/alarm_service.dart';
import 'screens/alarm_list_screen.dart';

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
    FlutterLocalNotificationsPlugin();
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

@pragma('vm:entry-point')
void notificationTapBackground(NotificationResponse response) {
  // background has no context
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);

  await Alarm.init();

  // await AlarmService.init(
  //   alarmRang: (alarmSettings) async {
  //     navigatorKey.currentState?.push(
  //       MaterialPageRoute(
  //         builder: (_) => AlarmRingingScreen(
  //           title: alarmSettings.notificationSettings?.title ?? '알람',
  //           body: alarmSettings.notificationSettings?.body ?? '기상 시간입니다.',
  //         ),
  //       ),
  //     );
  //   },
  //   alarmStopped: () async {
  //     // 알람 종료 시 처리 로직 (필요시)
  //   },
  // );

  const AndroidInitializationSettings initializationSettingsAndroid =
      AndroidInitializationSettings('@mipmap/ic_launcher');
  const InitializationSettings initializationSettings = InitializationSettings(
    android: initializationSettingsAndroid,
  );

  // await flutterLocalNotificationsPlugin.initialize(
  //   initializationSettings,
  //   onDidReceiveNotificationResponse: (details) {
  //     navigatorKey.currentState?.push(
  //       MaterialPageRoute(
  //         builder: (_) => AlarmRingingScreen(
  //           title: details.payload ?? '알람',
  //           body: '일어날 시간이에요!',
  //         ),
  //       ),
  //     );
  //   },
  //   onDidReceiveBackgroundNotificationResponse: notificationTapBackground,
  // );

  runApp(const AlarmApp());
  // runApp(const MaterialApp(
  //   home: UsageStatsScreen(),
  // ));
}

class AlarmApp extends StatelessWidget {
  const AlarmApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Next Up',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(scaffoldBackgroundColor: Colors.black),
      home: const AlarmListScreen(),
      navigatorKey: navigatorKey,
    );
  }
}
