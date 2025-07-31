import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../services/accessibility_manager.dart';

class BlockingOverlay extends StatelessWidget {
  const BlockingOverlay({super.key});

  @override
  Widget build(BuildContext context) {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: GestureDetector(
        onDoubleTap: () => AccessibilityManager.hideOverlayManually(),
        child: Scaffold(
          backgroundColor: Colors.black.withAlpha(200),
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: const [
                Icon(Icons.block, color: Colors.white, size: 80),
                SizedBox(height: 24),
                Text(
                  '이 앱은 현재 차단되어 있습니다',
                  style: TextStyle(color: Colors.white, fontSize: 20),
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
