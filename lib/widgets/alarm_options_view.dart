import 'package:flutter/material.dart';
import '../widgets/option_tile.dart';

class AlarmOptionsView extends StatelessWidget {
  final bool alarmSoundEnabled;
  final String selectedRingtoneName;
  final void Function(bool) onAlarmSoundToggle;
  final Future<void> Function() onSelectSound;

  final bool vibrationEnabled;
  final void Function(bool) onVibrationToggle;

  final String snoozeLabel;
  final Future<void> Function() onSelectSnooze;

  final double volume;
  final Future<void> Function() onSelectVolume;

  final bool fadeEnabled;
  final void Function(bool) onFadeToggle;

  const AlarmOptionsView({
    super.key,
    required this.alarmSoundEnabled,
    required this.selectedRingtoneName,
    required this.onAlarmSoundToggle,
    required this.onSelectSound,
    required this.vibrationEnabled,
    required this.onVibrationToggle,
    required this.snoozeLabel,
    required this.onSelectSnooze,
    required this.volume,
    required this.onSelectVolume,
    required this.fadeEnabled,
    required this.onFadeToggle,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        OptionTile(
          title: '알람음',
          subtitle: alarmSoundEnabled ? selectedRingtoneName : '사용 안 함',
          value: alarmSoundEnabled,
          onTap: alarmSoundEnabled ? onSelectSound : null,
          onSwitch: onAlarmSoundToggle,
        ),
        OptionTile(
          title: '진동',
          subtitle: vibrationEnabled ? '사용' : '사용 안 함',
          value: vibrationEnabled,
          onTap: null,
          onSwitch: onVibrationToggle,
        ),
        OptionTile(
          title: '스누즈 설정',
          subtitle: snoozeLabel,
          value: true,
          onTap: onSelectSnooze,
          onSwitch: null,
        ),
        OptionTile(
          title: '볼륨',
          subtitle: '${(volume * 100).round()}%',
          value: true,
          onTap: onSelectVolume,
          onSwitch: null,
        ),
        OptionTile(
          title: '점점 커지기',
          subtitle: fadeEnabled ? '사용' : '사용 안 함',
          value: fadeEnabled,
          onTap: null,
          onSwitch: onFadeToggle,
        ),
      ],
    );
  }
}
