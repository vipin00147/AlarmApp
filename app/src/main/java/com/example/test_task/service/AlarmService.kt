package com.example.test_task.service

import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import androidx.viewbinding.ViewBinding
import com.example.test_task.alarm.NotificationHelper
import com.example.test_task.service.ui.activities.AlarmTriggerActivity
import com.example.test_task.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AlarmService : Service() {

    var wakeLock: PowerManager.WakeLock? = null
    private var v: Vibrator? = null
    private var player: MediaPlayer? = null
    private var handler: Handler? = null
    private var crescendoRunnable: Runnable? = null
    private var sharedPref: SharedPreferences? = null

    companion object {
        var alarmTotalSecondPlayer : Long = 0
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_MUTE)
        registerReceiver(MuteActionReceiver, filter)

        var alarmId = -1
        if (intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1)

        startForeground(1, NotificationHelper(this, alarmId).deliverNotification())


         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val alarmActivityIntent = Intent(this, AlarmTriggerActivity::class.java)
            alarmActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            alarmActivityIntent.putExtra("alarmIdKey", alarmId)
            startActivity(alarmActivityIntent)
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        countdownTimer()
        playAlarm()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (handler != null && crescendoRunnable != null) handler!!.removeCallbacks(
            crescendoRunnable!!)

        if (player != null) {
            player!!.release()
            player = null
            v!!.cancel()
        }
        unregisterReceiver(MuteActionReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun playAlarm() {

        val alarmDefaultSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
        var alarmUserSelectedSound = alarmDefaultSound

        if (sharedPref != null && sharedPref!!.contains("ringtone")) {
            alarmUserSelectedSound =
                Uri.parse(sharedPref!!.getString("ringtone", alarmDefaultSound.toString()))
        }

        player = MediaPlayer()

        try {
            player!!.setDataSource(this, alarmUserSelectedSound!!)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            player!!.isLooping = true
            player!!.setAudioAttributes(audioAttributes)
            player!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        player!!.setOnPreparedListener { startPlayerOnPrepared() }
        vibrateAlarm()
    }

    private fun countdownTimer() {

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmApp::AlarmTriggerWakeLock")

        GlobalScope.launch(Dispatchers.Main) {
            val totalSeconds = TimeUnit.SECONDS.toSeconds(15)
            val tickSeconds = 1
            for (second in totalSeconds downTo tickSeconds) {
                alarmTotalSecondPlayer = second
                delay(1000)
            }

            stopAlarmService()
        }
    }

    private fun stopAlarmService() {
        stopSelf()
        /*val intent = Intent(AlarmTriggerActivity(), AlarmService::class.java)
        stopService(intent)*/
    }

    private fun startPlayerOnPrepared() {

        val MAX_VOLUME = 1.0f
        val KEY_CRESCENDO_TIME = "crescendoTime"

        val crescendoTimeStr = sharedPref!!.getString(KEY_CRESCENDO_TIME, "0")

        var crescendoTime = 0f
        if (crescendoTimeStr != null) crescendoTime = crescendoTimeStr.toInt().toFloat()

        if (crescendoTime == 0f) {
            player!!.start()
            return
        }

        player!!.setVolume(0f, 0f)

        val handlerThread = HandlerThread("HandlerThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val incrementPerSecond = 1 / crescendoTime

        crescendoRunnable = object : Runnable {

            var currentVol = 0.0f
            override fun run() {

                if (player == null) {
                    return
                }

                if (currentVol < MAX_VOLUME) {
                    currentVol += incrementPerSecond
                    player!!.setVolume(currentVol, currentVol)
                    handler!!.postDelayed(this, 1000)
                }
            }
        }

        player!!.start()
        handler!!.post(crescendoRunnable as Runnable)
    }

    private fun vibrateAlarm() {
        val KEY_VIBRATE = "vibrateEnabled"

        val vibrationEnabled = sharedPref!!.getBoolean(KEY_VIBRATE, true)

        v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrationEnabled) {

            val vibratePattern = longArrayOf(0, 500, 1000)
            val effect: VibrationEffect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                effect = VibrationEffect.createWaveform(vibratePattern, 0)
                v!!.vibrate(effect)
            } else {
                v!!.vibrate(vibratePattern, 0)
            }
        }
    }

    private val MuteActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Constants.ACTION_MUTE.equals(intent.action)) {
                muteAlarm()
            }
        }
    }

    private fun muteAlarm() {
        if (handler != null && crescendoRunnable != null) handler!!.removeCallbacks(
            crescendoRunnable!!)

        player!!.stop()
        v!!.cancel()
    }
}