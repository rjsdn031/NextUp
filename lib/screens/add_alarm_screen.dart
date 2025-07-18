import 'package:flutter/material.dart';
import '../models/alarm_model.dart';

class AddAlarmScreen extends StatefulWidget {
  const AddAlarmScreen({super.key});

  @override
  State<AddAlarmScreen> createState() => _AddAlarmScreenState();
}

class _AddAlarmScreenState extends State<AddAlarmScreen> {
  TimeOfDay selectedTime = const TimeOfDay(hour: 7, minute: 0);
  Set<DayOfWeek> selectedDays = {};

  bool skipHolidays = false;
  String alarmName = '';

  bool alarmSoundEnabled = true;
  String selectedRingtone = 'Classic Bell';
  final ringtoneOptions = ['Classic Bell'];

  bool vibrationEnabled = true;
  String vibrationPattern = '기본';
  final vibrationOptions = ['기본'];

  bool snoozeEnabled = true;
  String snoozeOption = '5분, 3회';
  final snoozeOptions = ['5분, 3회'];

  // alarm ringtone
  Future<void> _showRingtonePicker() async {
    final selected = await showDialog<String>(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: const Text('알람음 선택'),
          children: ringtoneOptions.map((tone) {
            return RadioListTile<String>(
              title: Text(tone),
              value: tone,
              groupValue: selectedRingtone,
              onChanged: (val) => Navigator.pop(context, val),
            );
          }).toList(),
        );
      },
    );

    if (selected != null && selected != selectedRingtone) {
      setState(() {
        selectedRingtone = selected;
      });
    }
  }

  // alarm vibration
  Future<void> _showVibrationPicker() async {
    final selected = await showDialog<String>(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: const Text('진동 패턴 선택'),
          children: vibrationOptions.map((option) {
            return RadioListTile<String>(
              title: Text(option),
              value: option,
              groupValue: vibrationPattern,
              onChanged: (val) => Navigator.pop(context, val),
            );
          }).toList(),
        );
      },
    );

    if (selected != null && selected != vibrationPattern) {
      setState(() => vibrationPattern = selected);
    }
  }

  // alarm snooze
  Future<void> _showSnoozePicker() async {
    final selected = await showDialog<String>(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: const Text('다시 울림 설정'),
          children: snoozeOptions.map((option) {
            return RadioListTile<String>(
              title: Text(option),
              value: option,
              groupValue: snoozeOption,
              onChanged: (val) => Navigator.pop(context, val),
            );
          }).toList(),
        );
      },
    );

    if (selected != null && selected != snoozeOption) {
      setState(() => snoozeOption = selected);
    }
  }

  void _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: selectedTime,
    );
    if (picked != null) setState(() => selectedTime = picked);
  }

  @override
  Widget build(BuildContext context) {
    final dayLabels = ['일', '월', '화', '수', '목', '금', '토'];

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: const Text('알람 추가'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Column(
        children: [
          // 시간 선택
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
          // 요일 선택
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: List.generate(7, (i) {
                final day = DayOfWeek.values[i];
                final label = dayLabels[i];
                final selected = selectedDays.contains(day);
                return GestureDetector(
                  onTap: () {
                    setState(() {
                      if (selected) {
                        selectedDays.remove(day);
                      } else {
                        selectedDays.add(day);
                      }
                    });
                  },
                  child: CircleAvatar(
                    radius: 18,
                    backgroundColor: selected ? Colors.blue : Colors.grey[700],
                    child: Text(
                      label,
                      style: const TextStyle(color: Colors.white),
                    ),
                  ),
                );
              }),
            ),
          ),
          const SizedBox(height: 8),
          Divider(color: Colors.grey[800]),
          SwitchListTile(
            title: const Text(
              '공휴일엔 알람 끄기',
              style: TextStyle(color: Colors.white),
            ),
            value: skipHolidays,
            onChanged: (val) => setState(() => skipHolidays = val),
            activeColor: Colors.blue,
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
          ListTile(
            onTap: alarmSoundEnabled ? _showRingtonePicker : null,
            title: const Text('알람음', style: TextStyle(color: Colors.white)),
            subtitle: Text(
              alarmSoundEnabled ? selectedRingtone : '사용 안 함',
              style: const TextStyle(color: Colors.grey),
            ),
            trailing: Switch(
              value: alarmSoundEnabled,
              onChanged: (val) => setState(() => alarmSoundEnabled = val),
              activeColor: Colors.blue,
            ),
          ),
          ListTile(
            onTap: vibrationEnabled ? _showVibrationPicker : null,
            title: const Text('진동', style: TextStyle(color: Colors.white)),
            subtitle: Text(
              vibrationEnabled ? vibrationPattern : '사용 안 함',
              style: const TextStyle(color: Colors.grey),
            ),
            trailing: Switch(
              value: vibrationEnabled,
              onChanged: (val) => setState(() => vibrationEnabled = val),
              activeColor: Colors.blue,
            ),
          ),
          ListTile(
            onTap: snoozeEnabled ? _showSnoozePicker : null,
            title: const Text('다시 울림', style: TextStyle(color: Colors.white)),
            subtitle: Text(
              snoozeEnabled ? snoozeOption : '사용 안 함',
              style: const TextStyle(color: Colors.grey),
            ),
            trailing: Switch(
              value: snoozeEnabled,
              onChanged: (val) => setState(() => snoozeEnabled = val),
              activeColor: Colors.blue,
            ),
          ),
          const Spacer(),
          Row(
            children: [
              Expanded(
                child: TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text(
                    '취소',
                    style: TextStyle(color: Colors.white70),
                  ),
                ),
              ),
              Expanded(
                child: TextButton(
                  onPressed: () {
                    final newAlarm = AlarmModel(
                      time: selectedTime,
                      days: selectedDays.toList(),
                      name: alarmName,
                      skipHolidays: skipHolidays,
                      vibration: vibrationEnabled,
                      snoozeEnabled: snoozeEnabled,
                      ringtone: selectedRingtone, // ex: 'Classic Bell'
                    );
                    Navigator.pop(context, newAlarm);
                  },
                  child: const Text(
                    '저장',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
