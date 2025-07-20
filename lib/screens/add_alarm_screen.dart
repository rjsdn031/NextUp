import 'package:flutter/material.dart';
import '../models/alarm_model.dart';
import '../widgets/day_selector.dart';
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
  bool skipHolidays = false;
  String alarmName = '';

  bool alarmSoundEnabled = true;
  String selectedRingtone = 'Classic Bell';

  bool vibrationEnabled = true;
  String vibrationPattern = '기본';

  bool snoozeEnabled = true;
  String snoozeOption = '5분, 3회';

  @override
  void initState() {
    super.initState();
    final alarm = widget.initialAlarm;
    if (alarm != null) {
      selectedTime = alarm.time;
      selectedDays = alarm.days.toSet();
      skipHolidays = alarm.skipHolidays;
      alarmName = alarm.name;
      alarmSoundEnabled = alarm.ringtone != '없음';
      selectedRingtone = alarm.ringtone;
      vibrationEnabled = alarm.vibration;
      snoozeEnabled = alarm.snoozeEnabled;
    }
  }

  void _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: selectedTime,
    );
    if (picked != null) setState(() => selectedTime = picked);
  }

  void _saveAlarm() {
    final updatedAlarm = AlarmModel(
      time: selectedTime,
      days: selectedDays.toList(),
      name: alarmName,
      skipHolidays: skipHolidays,
      vibration: vibrationEnabled,
      snoozeEnabled: snoozeEnabled,
      ringtone: selectedRingtone,
      enabled: widget.initialAlarm?.enabled ?? true,
    );

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
      body: Column(
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
          SwitchListTile(
            title: const Text('공휴일엔 알람 끄기', style: TextStyle(color: Colors.white)),
            value: skipHolidays,
            onChanged: (val) => setState(() => skipHolidays = val),
            activeColor: Colors.grey,
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
            value: alarmSoundEnabled,
            subtitle: alarmSoundEnabled ? selectedRingtone : '사용 안 함',
            onTap: alarmSoundEnabled
                ? () async {
              final selected = await showOptionDialog(
                context,
                '알람음 선택',
                ['Classic Bell'],
                selectedRingtone,
              );
              if (selected != null) {
                setState(() => selectedRingtone = selected);
              }
            }
                : null,
            onSwitch: (val) => setState(() => alarmSoundEnabled = val),
          ),
          OptionTile(
            title: '진동',
            value: vibrationEnabled,
            subtitle: vibrationEnabled ? vibrationPattern : '사용 안 함',
            onTap: vibrationEnabled
                ? () async {
              final selected = await showOptionDialog(
                context,
                '진동 패턴 선택',
                ['기본'],
                vibrationPattern,
              );
              if (selected != null) {
                setState(() => vibrationPattern = selected);
              }
            }
                : null,
            onSwitch: (val) => setState(() => vibrationEnabled = val),
          ),
          OptionTile(
            title: '다시 울림',
            value: snoozeEnabled,
            subtitle: snoozeEnabled ? snoozeOption : '사용 안 함',
            onTap: snoozeEnabled
                ? () async {
              final selected = await showOptionDialog(
                context,
                '다시 울림 설정',
                ['5분, 3회'],
                snoozeOption,
              );
              if (selected != null) {
                setState(() => snoozeOption = selected);
              }
            }
                : null,
            onSwitch: (val) => setState(() => snoozeEnabled = val),
          ),
          if (widget.initialAlarm != null)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Align(
                alignment: Alignment.centerLeft,
                child: TextButton.icon(
                  onPressed: () async {
                    final confirmed = await showDialog<bool>(
                      context: context,
                      builder: (ctx) => AlertDialog(
                        title: const Text('알람 삭제'),
                        content: const Text('정말 이 알람을 삭제하시겠습니까?'),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(ctx, false),
                            child: const Text('취소'),
                          ),
                          TextButton(
                            onPressed: () => Navigator.pop(ctx, true),
                            child: const Text('삭제', style: TextStyle(color: Colors.red)),
                          ),
                        ],
                      ),
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
          const Spacer(),
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
        ],
      ),
    );
  }
}
