class AlarmModel {
  final String time;
  final List<String> days;
  bool enabled;

  AlarmModel({
    required this.time,
    required this.days,
    this.enabled = true,
  });
}