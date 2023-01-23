package com.example.test_task.service.ui.activities

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import com.example.test_task.alarm.NotificationHelper
import com.example.test_task.base.BaseActivity
import com.example.test_task.databinding.ActivityAlarmTriggerBinding
import com.example.test_task.service.AlarmService
import com.example.test_task.service.AlarmService.Companion.alarmTotalSecondPlayer
import com.example.test_task.utils.Constants
import com.ncorti.slidetoact.SlideToActView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Runnable
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AlarmTriggerActivity : BaseActivity<ActivityAlarmTriggerBinding>() {

    var wakeLock: WakeLock? = null
    private var sharedPref: SharedPreferences? = null
    private var handler: Handler? = null
    private var actionBtnPref: String? = null
    private var silenceRunnable: Runnable? = null

    override fun createBinding(): ActivityAlarmTriggerBinding {
        return ActivityAlarmTriggerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmApp::AlarmTriggerWakeLock")

        wakeLock?.acquire(3000)

        turnOnScreen()

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(PowerBtnReceiver, filter)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val btnDismissAlarm = binding.btnDismissAlarm

        val intent = intent

        var alarmId = -1
        if (intent.hasExtra("alarmIdKey")) alarmId = intent.getIntExtra("alarmIdKey", -1)

        btnDismissAlarm.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(slideToActView: SlideToActView) {
                stopAlarmService()
            }
        }

        silenceTimeout(alarmId)
        countdownTimer()

    }

    private fun countdownTimer() {
        GlobalScope.launch(Dispatchers.Main) {
            val totalSeconds = TimeUnit.SECONDS.toSeconds(alarmTotalSecondPlayer.toLong())
            val tickSeconds = 1
            for (second in totalSeconds downTo tickSeconds) {
                delay(1000)
            }

            stopAlarmService()
        }
    }

    private fun turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            val win = window
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }
    }

    private val PowerBtnReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent != null && intent.action != null) {
                if (intent.action == Intent.ACTION_SCREEN_OFF) {

                    actionBtnPref = sharedPref!!.getString("power_btn_action", Constants.ACTION_DO_NOTHING)
                    if (actionBtnPref != null) actionBtnHandler(actionBtnPref!!)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
        unregisterReceiver(PowerBtnReceiver)
    }

    private fun actionBtnHandler(action: String) {
        when (action) {
            Constants.ACTION_MUTE ->
                sendBroadcast(Intent().setAction(Constants.ACTION_MUTE))
            Constants.ACTION_DISMISS -> stopAlarmService()
        }
    }

    fun stopAlarmService() {
        wakeLock!!.release()
        val intent = Intent(this@AlarmTriggerActivity, AlarmService::class.java)
        stopService(intent)

        if (handler != null && silenceRunnable != null) handler!!.removeCallbacks(silenceRunnable!!)
        finish()
    }

    fun silenceTimeout(alarmId: Int) {
        val KEY_SILENCE_TIMEOUT = "silenceTimeout"

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val silenceTimeStr = sharedPref.getString(KEY_SILENCE_TIMEOUT, "0")

        var silenceTimeoutInt = 0
        if (silenceTimeStr != null) silenceTimeoutInt = silenceTimeStr.toInt()

        if (silenceTimeoutInt == 0) return

        handler = Handler(mainLooper)
        silenceRunnable = Runnable {
            val nh = NotificationHelper(applicationContext, alarmId)
            stopAlarmService()
        }

        handler!!.postDelayed(silenceRunnable!!,
            (silenceTimeoutInt * 60000).toLong())
    }
}