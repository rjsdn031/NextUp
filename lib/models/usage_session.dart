class UsageSession {
  final String packageName;
  final DateTime startTime;
  final DateTime endTime;

  UsageSession({
    required this.packageName,
    required this.startTime,
    required this.endTime,
  });

  Duration get duration => endTime.difference(startTime);
}