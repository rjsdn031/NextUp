import 'package:alarm/alarm.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../utils/alarm_vibrator.dart';
import '../utils/alarm_sound_player.dart';
import '../widgets/swipe_to_dismiss_button.dart';

class AlarmRingingScreen extends StatefulWidget {
  final String title;
  final String body;
  final int alarmId;

  const AlarmRingingScreen({
    super.key,
    required this.title,
    required this.body,
    required this.alarmId,
  });

  @override
  State<AlarmRingingScreen> createState() => _AlarmRingingScreenState();
}

class _AlarmRingingScreenState extends State<AlarmRingingScreen> {
  late TimeOfDay now;

  @override
  void initState() {
    super.initState();
    now = TimeOfDay.now();
  }

  @override
  Widget build(BuildContext context) {
    final date = DateFormat('M월 d일 EEEE', 'ko').format(DateTime.now());

    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        child: Stack(
          alignment: Alignment.center,
          children: [
            Align(
              alignment: Alignment.topCenter,
              child: SizedBox(
                width: double.infinity,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const SizedBox(height: 120),
                    Text(
                      now.format(context),
                      style: const TextStyle(
                        fontSize: 64,
                        fontWeight: FontWeight.w600,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Text(
                      date,
                      style: const TextStyle(fontSize: 20, color: Colors.white70),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      widget.body,
                      style: const TextStyle(fontSize: 18, color: Colors.white),
                    ),
                    const Spacer(),
                    SwipeToDismissButton(
                      onDismiss: () async {
                        await Alarm.stop(widget.alarmId);
                        if (mounted) Navigator.of(context).pop();
                      },
                    ),
                    const SizedBox(height: 120),
                  ],
                ),
              ),
            ),

            // 스누즈 버튼 (하단 고정)
            Positioned(
              bottom: 40,
              left: 0,
              right: 0,
              child: Center(
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white12,
                    padding: const EdgeInsets.symmetric(
                        horizontal: 24, vertical: 12),
                  ),
                  onPressed: () {
                    // snooze 처리
                  },
                  child: const Text(
                    '5분 후 다시 알림',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}