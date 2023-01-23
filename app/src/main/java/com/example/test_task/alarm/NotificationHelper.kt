package com.example.test_task.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test_task.BuildConfig
import com.example.test_task.R
import com.example.test_task.broadcast.AlarmBroadcastReceiver
import com.example.test_task.service.ui.activities.AlarmTriggerActivity
import com.example.test_task.utils.MyApplication
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val mContext: Context, var mAlarmId: Int) {
    private var mNotifyManager: NotificationManager? = null

    fun createNotificationChannel() {
        mNotifyManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationChannel = NotificationChannel(
            PRIMARY_CHANNEL_ID, "AlarmApp", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(true)
        notificationChannel.description = mContext.getString(R.string.notification_channel_description)
        mNotifyManager!!.createNotificationChannel(notificationChannel)

    }

    fun deliverNotification(): Notification {
        val fullScreenIntent = Intent(mContext, AlarmTriggerActivity::class.java)
        Log.i(TAG,
            "deliverNotification: Putting alarmIdKey: $mAlarmId")
        fullScreenIntent.putExtra("alarmIdKey", mAlarmId)
        val fullScreenPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(mContext, 0, fullScreenIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(mContext, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        val sdf = SimpleDateFormat("hh:mm aa",
            Locale.getDefault())
        val formattedTime = sdf.format(System.currentTimeMillis())
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            mContext, PRIMARY_CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.alarm))
            .setContentText(formattedTime)
            .setSmallIcon(R.drawable.ic_alarm)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setFullScreenIntent(
            fullScreenPendingIntent,
            true) else  // Set on notification click intent for pre oreo
            builder.setContentIntent(fullScreenPendingIntent)


        return builder.build()
    }

    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_channel_id"
        private const val TAG = "NotificationHelper"
    }
}