package com.safelink.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** FR-UX-04/05. Always call stop() when the user leaves the screen. */
class AlarmController(private val context: Context) {
    private var ringtone: Ringtone? = null
    private var previousAlarmVolume: Int? = null

    fun start(sound: Boolean = true, haptics: Boolean = true, volumeCapPercent: Int = 100) {
        if (haptics) vibrate()
        if (sound && volumeCapPercent > 0) play(volumeCapPercent)
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null
        vibrator()?.cancel()
        restoreVolume()
    }

    private fun vibrate() {
        val pattern = longArrayOf(0, 400, 200, 400, 200, 800)
        val v = vibrator() ?: return
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun play(volumeCapPercent: Int) {
        if (Build.VERSION.SDK_INT < 28) applyStreamCap(volumeCapPercent)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return
        ringtone = RingtoneManager.getRingtone(context, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= 28) {
                isLooping = true
                volume = (volumeCapPercent / 100f).coerceIn(0.05f, 1f)
            }
            play()
        }
    }

    private fun applyStreamCap(percent: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        previousAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, (max * percent / 100).coerceIn(0, max), 0)
    }

    private fun restoreVolume() {
        val previous = previousAlarmVolume ?: return
        previousAlarmVolume = null
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.setStreamVolume(AudioManager.STREAM_ALARM, previous, 0)
    }

    private fun vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}
