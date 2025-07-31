import 'package:flutter/material.dart';

import '../services/accessibility_manager.dart';

class BlockingOverlay extends StatelessWidget {
  const BlockingOverlay({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: GestureDetector(
        onDoubleTap: () => AccessibilityManager.hideOverlayManually(),
        child: Scaffold(
          backgroundColor: Colors.white.withAlpha(200),
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: const [
                Icon(Icons.block, color: Colors.black, size: 80),
                SizedBox(height: 24),
                Text(
                  '이 앱은 현재 차단되어 있습니다',
                  style: TextStyle(color: Colors.black, fontSize: 20),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
