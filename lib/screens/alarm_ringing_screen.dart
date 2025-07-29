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
  final AlarmVibrator _vibrator = AlarmVibrator();
  final AlarmSoundPlayer _soundPlayer = AlarmSoundPlayer();

  @override
  void initState() {
    super.initState();
    _vibrator.start();
    _soundPlayer.start();
  }

  @override
  void dispose() {
    _vibrator.stop();
    _soundPlayer.stop();
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
                _vibrator.stop();
                _soundPlayer.stop();
                await Alarm.stop(widget.alarmId); // 여기서 ID 직접 사용
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
