import 'package:alarm/alarm.dart';
import 'package:flutter/material.dart';
import '../utils/alarm_vibrator.dart';
import '../utils/alarm_sound_player.dart';

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

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              widget.title,
              style: const TextStyle(fontSize: 32, color: Colors.white),
            ),
            const SizedBox(height: 20),
            Text(
              widget.body,
              style: const TextStyle(fontSize: 18, color: Colors.white70),
            ),
            const SizedBox(height: 40),
            ElevatedButton(
              onPressed: () async {
                await Alarm.stop(widget.alarmId);
                if (mounted) Navigator.of(context).pop();
              },
              child: const Text('알람 끄기'),
            ),
          ],
        ),
      ),
    );
  }
}
