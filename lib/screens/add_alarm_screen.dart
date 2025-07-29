import 'package:flutter/material.dart';
import 'package:nextup/widgets/bottom_action_buttons.dart';
import '../data/alarm_constants.dart';
import '../models/alarm_model.dart';
import '../services/alarm_service.dart';
import '../widgets/alarm_options_view.dart';
import '../widgets/alarm_time_picker.dart';
import '../widgets/alarm_name_field.dart';
import '../widgets/day_selector.dart';
import '../widgets/delete_alarm_button.dart';
import '../widgets/option_tile.dart';
import '../utils/dialog_utils.dart';

class AddAlarmScreen extends StatefulWidget {
  final AlarmModel? initialAlarm;
  final int? index;

  const AddAlarmScreen({super.key, this.initialAlarm, this.index});

  @override
  State<AddAlarmScreen> createState() => _AddAlarmScreenState();
}

class _AddAlarmScreenState extends State<AddAlarmScreen> {
  TimeOfDay selectedTime = const TimeOfDay(hour: 7, minute: 0);
  Set<DayOfWeek> selectedDays = {};
  bool skipHolidays = true;

  String alarmName = '';
  bool vibrationEnabled = true;
  double volume = 1.0;
  int fadeDuration = 0;
  bool loopAudio = true;

  bool alarmSoundEnabled = true;
  String selectedRingtoneName = defaultRingtoneName;
  String selectedRingtonePath = defaultRingtonePath;

  bool snoozeEnabled = false;
  int snoozeInterval = defaultSnoozeInterval;
  int maxSnoozeCount = defaultMaxSnoozeCount;

  @override
  void initState() {
    super.initState();
    _initializeFromAlarm(widget.initialAlarm);
  }

  void _initializeFromAlarm(AlarmModel? alarm) {
    if (alarm == null) return;
    selectedTime = alarm.time;
    selectedDays = alarm.days.toSet();
    skipHolidays = alarm.skipHolidays;
    alarmName = alarm.name;
    vibrationEnabled = alarm.vibration;
    fadeDuration = alarm.fadeDuration;
    volume = alarm.volume;
    loopAudio = alarm.loopAudio;
    alarmSoundEnabled = alarm.alarmSoundEnabled;
    selectedRingtoneName = alarm.ringtoneName;
    selectedRingtonePath = alarm.assetAudioPath;
    // snoozeEnabled = false;
  }

  void _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: selectedTime,
    );
    if (picked != null) setState(() => selectedTime = picked);
  }

  void _saveAlarm() async {
    final updatedAlarm = _buildAlarmModel();

    if (updatedAlarm.enabled) {
      await AlarmService.scheduleAlarm(updatedAlarm);
    }

    if (widget.initialAlarm != null && widget.index != null) {
      Navigator.pop(context, {'alarm': updatedAlarm, 'index': widget.index});
    } else {
      Navigator.pop(context, updatedAlarm);
    }
  }

  AlarmModel _buildAlarmModel() {
    final alarmId = widget.index ?? DateTime.now().hashCode;
    return AlarmModel(
      id: alarmId,
      time: selectedTime,
      days: selectedDays.toList(),
      skipHolidays: skipHolidays,
      name: alarmName,
      vibration: vibrationEnabled,
      fadeDuration: fadeDuration,
      volume: volume,
      loopAudio: loopAudio,
      alarmSoundEnabled: alarmSoundEnabled,
      ringtoneName: selectedRingtoneName,
      assetAudioPath: selectedRingtonePath,
      enabled: widget.initialAlarm?.enabled ?? true,
      snoozeEnabled: snoozeEnabled,
      snoozeInterval: snoozeInterval,
      maxSnoozeCount: maxSnoozeCount,
    );
  }

  String get selectedSnoozeLabel =>
      formatSnoozeOption(snoozeInterval, maxSnoozeCount);

  Future<void> _onSelectAlarmSound() async {
    final selected = await showOptionDialog(
      context,
      '알람음 선택',
      alarmSounds.keys.toList(),
      selectedRingtoneName,
    );
    if (selected != null) {
      setState(() {
        selectedRingtoneName = selected;
        selectedRingtonePath = alarmSounds[selected]!;
      });
    }
  }

  void _onToggleVibration(bool val) {
    setState(() => vibrationEnabled = val);
  }

  Future<void> _onSelectSnooze() async {
    final options = snoozeOptions
        .map((opt) => formatSnoozeOption(opt['interval']!, opt['count']!))
        .toList();
    final selected = await showOptionDialog(
      context,
      '스누즈 설정 선택',
      options,
      selectedSnoozeLabel,
    );
    if (selected != null) {
      final idx = options.indexOf(selected);
      final selectedOption = snoozeOptions[idx];
      setState(() {
        snoozeInterval = selectedOption['interval']!;
        maxSnoozeCount = selectedOption['count']!;
      });
    }
  }

  Future<void> _onSelectVolume() async {
    final selected = await showSliderDialog(
      context: context,
      title: '볼륨 설정',
      initial: volume,
      min: 0,
      max: 1,
      divisions: 10,
      formatValue: (v) => '${(v * 100).round()}%',
    );
    if (selected != null) setState(() => volume = selected);
  }

  void _onToggleFade(bool val) {
    setState(() => fadeDuration = val ? 30 : 0);
  }

  void _onToggleAlarmSound(bool val) {
    setState(() => alarmSoundEnabled = val);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: const Text('알람 추가'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            AlarmTimePicker(time: selectedTime, onTap: _pickTime),
            DaySelector(
              selectedDays: selectedDays,
              onChanged: (Set<DayOfWeek> days) {
                setState(() => selectedDays = days);
              },
            ),
            const SizedBox(height: 14),
            OptionTile(
              title: '공휴일엔 알람 끄기',
              subtitle: skipHolidays ? '사용' : '사용 안 함',
              value: skipHolidays,
              onTap: null,
              onSwitch: (val) => setState(() => skipHolidays = val),
            ),
            AlarmNameField(
              initialValue: alarmName,
              onChanged: (val) => alarmName = val,
            ),
            const SizedBox(height: 14),
            AlarmOptionsView(
              alarmSoundEnabled: alarmSoundEnabled,
              selectedRingtoneName: selectedRingtoneName,
              onAlarmSoundToggle: _onToggleAlarmSound,
              onSelectSound: _onSelectAlarmSound,
              vibrationEnabled: vibrationEnabled,
              onVibrationToggle: _onToggleVibration,
              snoozeLabel: selectedSnoozeLabel,
              onSelectSnooze: _onSelectSnooze,
              volume: volume,
              onSelectVolume: _onSelectVolume,
              fadeEnabled: fadeDuration > 0,
              onFadeToggle: _onToggleFade,
            ),

            if (widget.initialAlarm != null)
              DeleteAlarmButton(
                index: widget.index!,
                onDelete: () {
                  Navigator.pop(context, {
                    'delete': true,
                    'index': widget.index,
                  });
                },
              ),
            const SizedBox(height: 24),
            BottomActionButtons(
              onCancel: () => Navigator.pop(context),
              onSave: _saveAlarm,
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}
