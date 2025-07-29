import 'package:alarm/alarm.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:nextup/screens/alarm_ringing_screen.dart';
import 'package:nextup/screens/usage_stats_screen.dart';
import 'package:nextup/services/alarm_service.dart';
import 'package:nextup/services/navigation_service.dart';
import 'screens/alarm_list_screen.dart';

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
    FlutterLocalNotificationsPlugin();

@pragma('vm:entry-point')
void notificationTapBackground(NotificationResponse response) {
  // background has no context
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);

  await Alarm.init();
  await AlarmService.init();
  AlarmService.startListening();

  // const AndroidInitializationSettings initializationSettingsAndroid =
  //     AndroidInitializationSettings('@mipmap/ic_launcher');
  // const InitializationSettings initializationSettings = InitializationSettings(
  //   android: initializationSettingsAndroid,
  // );

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
      navigatorKey: NavigationService.navigatorKey,
    );
  }
}
