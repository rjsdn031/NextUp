import 'package:audioplayers/audioplayers.dart';

class AlarmSoundPlayer {
  final AudioPlayer _player = AudioPlayer();

  /// start alarm sound
  Future<void> start() async {
    await _player.setReleaseMode(ReleaseMode.loop);
    await _player.play(AssetSource('sounds/test_sound.mp3'));
  }

  /// stop alarm
  Future<void> stop() async {
    await _player.stop();
  }

  /// isPlaying?
  Future<bool> isPlaying() async {
    return _player.state == PlayerState.playing;
  }
}