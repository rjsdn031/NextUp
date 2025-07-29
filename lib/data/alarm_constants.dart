const Map<String, String> alarmSounds = {
  'Classic Bell': 'assets/sounds/test_sound.mp3',
  'Soft Piano': 'assets/sounds/soft_piano.mp3',
  'Nature Wind': 'assets/sounds/nature_wind.mp3',
};

const defaultRingtoneName = 'Classic Bell';
const defaultRingtonePath = 'assets/sounds/test_sound.mp3';

const snoozeOptions = [
  {'interval': 5, 'count': 3},
  {'interval': 5, 'count': 5},
  {'interval': 5, 'count': -1},
  {'interval': 10, 'count': 3},
  {'interval': 10, 'count': 5},
  {'interval': 10, 'count': -1},
  {'interval': 15, 'count': 3},
  {'interval': 15, 'count': 5},
  {'interval': 15, 'count': -1},
];

const defalutSnoozeInterval = 3;
const defaultMaxSnoozeCount = 5;

String formatSnoozeOption(int interval, int count) {
  return count == -1 ? '$interval분 × 계속반복' : '$interval분 × $count회';
}