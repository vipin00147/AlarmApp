package com.example.test_task.broadcast

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.test_task.BuildConfig
import com.example.test_task.service.AlarmService
import com.example.test_task.service.ReSchedAlarmService

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val jobServiceIntent = Intent(context, ReSchedAlarmService::class.java)
            ReSchedAlarmService.enqueueWork(context, jobServiceIntent)
        } else if (intent.action == ACTION_DISMISS) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmId = intent.getIntExtra("alarmIdKey", -1)

            val alarmCancelIntent = Intent(context, AlarmService::class.java)

            val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getForegroundService(context, alarmId, alarmCancelIntent, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getForegroundService(context, alarmId, alarmCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getService(context, alarmId, alarmCancelIntent, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getService(context, alarmId, alarmCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            }

            Toast.makeText(context, "Notification", Toast.LENGTH_SHORT).show()

            alarmManager.cancel(pendingIntent)

            val mNotifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyManager.cancel(alarmId)
        }
    }

    companion object {
        val ACTION_DISMISS: String = BuildConfig.APPLICATION_ID + ".ACTION_DISMISS"
        private const val TAG = "AlarmBroadcastReceiver"
    }
}
