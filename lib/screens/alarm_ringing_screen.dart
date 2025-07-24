import 'package:flutter/material.dart';

class AlarmRingingScreen extends StatelessWidget {
  final String title;
  final String body;

  const AlarmRingingScreen({super.key, required this.title, required this.body});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(title, style: const TextStyle(fontSize: 32, color: Colors.white)),
            const SizedBox(height: 20),
            Text(body, style: const TextStyle(fontSize: 18, color: Colors.white70)),
            const SizedBox(height: 40),
            ElevatedButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('알람 끄기'),
            ),
          ],
        ),
      ),
    );
  }
}