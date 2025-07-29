import 'package:flutter/material.dart';
import '../models/alarm_model.dart';
import '../services/alarm_service.dart';
import '../widgets/day_selector.dart';
import '../widgets/option_tile.dart';
import '../utils/dialog_utils.dart';

const Map<String, String> alarmSounds = {
  'Classic Bell': 'assets/sounds/test_sound.mp3',
  // 'Soft Piano': 'assets/sounds/soft_piano.mp3',
  // 'Nature Wind': 'assets/sounds/nature_wind.mp3',
};

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
  String selectedRingtoneName = 'Classic Bell';
  String selectedRingtonePath = 'assets/sounds/test_sound.mp3';

  bool snoozeEnabled = false;
  int snoozeInterval = 5;
  int maxSnoozeCount = 3;

  @override
  void initState() {
    super.initState();
    final alarm = widget.initialAlarm;
    if (alarm != null) {
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
      snoozeEnabled = false;
      snoozeInterval = 5;
      maxSnoozeCount = 3;
    }
  }

  void _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: selectedTime,
    );
    if (picked != null) setState(() => selectedTime = picked);
  }

  void _saveAlarm() async {
    final alarmId = widget.index ?? DateTime.now().hashCode;

    final updatedAlarm = AlarmModel(
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

    if (updatedAlarm.enabled) {
      await AlarmService.scheduleAlarm(updatedAlarm);
    }

    if (widget.initialAlarm != null && widget.index != null) {
      Navigator.pop(context, {'alarm': updatedAlarm, 'index': widget.index});
    } else {
      Navigator.pop(context, updatedAlarm);
    }
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
              GestureDetector(
                onTap: _pickTime,
                child: Container(
                  padding: const EdgeInsets.symmetric(vertical: 24),
                  alignment: Alignment.center,
                  child: Text(
                    selectedTime.format(context),
                    style: const TextStyle(fontSize: 48, color: Colors.white),
                  ),
                ),
              ),
              DaySelector(
                selectedDays: selectedDays,
                onChanged: (Set<DayOfWeek> days) {
                  setState(() => selectedDays = days);
                },
              ),
              const SizedBox(height: 8),
              Divider(color: Colors.grey[800]),
              OptionTile(
                title: '공휴일엔 알람 끄기',
                subtitle: skipHolidays ? '사용' : '사용 안 함',
                value: skipHolidays,
                onTap: null,
                onSwitch: (val) => setState(() => skipHolidays = val),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: TextField(
                  style: const TextStyle(color: Colors.white),
                  decoration: const InputDecoration(
                    labelText: '알람 이름',
                    labelStyle: TextStyle(color: Colors.grey),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.grey),
                    ),
                  ),
                  onChanged: (val) => alarmName = val,
                ),
              ),
              OptionTile(
                title: '알람음',
                subtitle: alarmSoundEnabled
                    ? selectedRingtoneName
                    : '사용 안 함',
                value: alarmSoundEnabled,
                onTap: alarmSoundEnabled
                    ? () async {
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
                    : null,
                onSwitch: (val) => setState(() => alarmSoundEnabled = val),
              ),
              OptionTile(
                title: '진동',
                value: vibrationEnabled,
                subtitle: vibrationEnabled ? '사용' : '사용 안 함',
                onTap: null,
                onSwitch: (val) => setState(() => vibrationEnabled = val),
              ),
              OptionTile(
                title: '다시 울림',
                subtitle: snoozeEnabled ? '사용' : '사용 안 함',
                value: snoozeEnabled,
                onTap: null,
                onSwitch: (val) => setState(() => snoozeEnabled = val),
              ),
              if (snoozeEnabled) ...[
                OptionTile(
                  title: '스누즈 간격',
                  subtitle: '$snoozeInterval분',
                  value: true,
                  onTap: () async {
                    final selected = await showSliderDialog(
                      context: context,
                      title: '스누즈 간격 (분)',
                      initial: snoozeInterval.toDouble(),
                      min: 1,
                      max: 30,
                      divisions: 29,
                      formatValue: (v) => '${v.round()}분',
                    );
                    if (selected != null) setState(() => snoozeInterval = selected.round());
                  },
                  onSwitch: null,
                ),
                OptionTile(
                  title: '최대 스누즈 횟수',
                  subtitle: '$maxSnoozeCount회',
                  value: true,
                  onTap: () async {
                    final selected = await showSliderDialog(
                      context: context,
                      title: '최대 스누즈 횟수',
                      initial: maxSnoozeCount.toDouble(),
                      min: 1,
                      max: 10,
                      divisions: 9,
                      formatValue: (v) => '${v.round()}회',
                    );
                    if (selected != null) setState(() => maxSnoozeCount = selected.round());
                  },
                  onSwitch: null,
                ),
              ],
              OptionTile(
                title: '볼륨',
                value: true,
                subtitle: '${(volume * 100).round()}%',
                onTap: () async {
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
                },
                onSwitch: null,
              ),
              OptionTile(
                title: '점점 커지기',
                value: fadeDuration > 0,
                subtitle: fadeDuration > 0 ? '$fadeDuration초 동안 증가' : '사용 안 함',
                onTap: () async {
                  final selected = await showSliderDialog(
                    context: context,
                    title: '점점 커지는 시간',
                    initial: fadeDuration.toDouble(),
                    min: 0,
                    max: 30,
                    divisions: 6,
                    formatValue: (v) => '${v.round()}초',
                  );
                  if (selected != null) setState(() => fadeDuration = selected.round());
                },
                onSwitch: (val) => setState(() => fadeDuration = val ? 10 : 0),
              ),
              if (widget.initialAlarm != null)
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Align(
                    alignment: Alignment.centerLeft,
                    child: TextButton.icon(
                      onPressed: () async {
                        final confirmed = await showConfirmDialog(
                          context: context,
                          title: '알람 삭제',
                          content: '정말 이 알람을 삭제하시겠습니까?',
                          confirmText: '삭제',
                        );

                        if (confirmed == true) {
                          Navigator.pop(context, {
                            'delete': true,
                            'index': widget.index,
                          });
                        }
                      },
                      icon: const Icon(Icons.delete, color: Colors.red),
                      label: const Text('알람 삭제', style: TextStyle(color: Colors.red)),
                    ),
                  ),
                ),
              const SizedBox(height: 24),
              Row(
                children: [
                  Expanded(
                    child: TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('취소', style: TextStyle(fontSize: 18, color: Colors.white70)),
                    ),
                  ),
                  Expanded(
                    child: TextButton(
                      onPressed: _saveAlarm,
                      child: const Text('저장', style: TextStyle(fontSize: 18, color: Colors.white)),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
            ],
          ),
        )

    );
  }
}

